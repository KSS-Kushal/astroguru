package com.kss.astrologer.services;

import com.kss.astrologer.dto.BookingAppointmentDto;
import com.kss.astrologer.dto.CallSessionDto;
import com.kss.astrologer.dto.ChatSessionDto;
import com.kss.astrologer.events.BookingApprovedEvent;
import com.kss.astrologer.events.BookingCancelledEvent;
import com.kss.astrologer.events.BookingRequestEvent;
import com.kss.astrologer.events.SessionCreatedEvent;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.*;
import com.kss.astrologer.repository.AstrologerRepository;
import com.kss.astrologer.repository.BookingAppointmentRepository;
import com.kss.astrologer.repository.BookingConfigRepository;
import com.kss.astrologer.request.CreateBookingRequest;
import com.kss.astrologer.types.BookingStatus;
import com.kss.astrologer.types.BookingType;
import com.kss.astrologer.types.ChatStatus;
import com.kss.astrologer.types.SessionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    private final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingAppointmentRepository bookingAppointmentRepository;

    @Autowired
    private BookingConfigRepository bookingConfigRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AstrologerRepository astrologerRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public BookingAppointment bookAppointment(CreateBookingRequest request, UUID userId) {
        User user = userService.getById(userId);
        AstrologerDetails astrologer = astrologerRepository.findByUserId(request.getAstrologerId())
                        .orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND, "Astrologer details not found"));

        LocalDate appointmentDate = request.getAppointmentDate();

        if (appointmentDate == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Appointment date is required");
        }

        // disallow booking in the past
        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Appointment date cannot be in the past");
        }

        // 2. Find BookingConfig for astrologer (via astrologer Id)
        Optional<BookingConfig> configOpt =
                bookingConfigRepository.findByAstrologer_Id(astrologer.getId());

        int bookingLimit = getBookingLimit(configOpt, appointmentDate);

        // 4. Check daily booking limit
        long existingBookings = bookingAppointmentRepository
                .countByAstrologer_IdAndAppointmentDate(astrologer.getUser().getId(), appointmentDate);

        if (existingBookings >= bookingLimit) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "Booking limit reached for this astrologer on this date");
        }

        Wallet userWallet = user.getWallet();

        if(request.getBookingType() == BookingType.ONLINE) {
            double totalCost = calculateTotalCost(astrologer, request.getSessionType(),
                    request.getAppointmentDuration());
            double balance = userWallet.getBalance() != null ? userWallet.getBalance() : 0.0;
            double lockedBalance = userWallet.getLockedBalance() != null ? userWallet.getLockedBalance() : 0.0;
            double userBalance = balance - lockedBalance;
            if (userBalance < totalCost) throw new CustomException("Insufficient Balance");
            this.walletService.addLockedBalance(userWallet.getId(), totalCost);
            int otp = this.otpService.generateOtp();
            BookingAppointment appointment = BookingAppointment.builder()
                    .user(user)
                    .astrologer(astrologer.getUser())
                    .reason(request.getReason())
                    .appointmentDate(request.getAppointmentDate())
                    .appointmentDuration(request.getAppointmentDuration())
                    .totalCost(totalCost)
                    .otp(otp)
                    .status(BookingStatus.PENDING)
                    .bookingType(BookingType.ONLINE)
                    .sessionType(request.getSessionType())
                    .build();

            BookingAppointment saved = bookingAppointmentRepository.save(appointment);
            // 🔥 Publish event
            eventPublisher.publishEvent(
                    new BookingRequestEvent(
                            appointment.getId(),
                            appointment.getUser().getId(),
                            appointment.getAstrologer().getId()
                    )
            );
            return saved;
        }
        BookingAppointment appointment = BookingAppointment.builder()
                .user(user)
                .astrologer(astrologer.getUser())
                .reason(request.getReason())
                .appointmentDate(request.getAppointmentDate())
                .appointmentDuration(request.getAppointmentDuration())
                .status(BookingStatus.PENDING)
                .bookingType(BookingType.OFFLINE)
                .sessionType(request.getSessionType())
                .build();
        BookingAppointment saved = bookingAppointmentRepository.save(appointment);

        // 🔥 Publish event
        eventPublisher.publishEvent(
                new BookingRequestEvent(
                        appointment.getId(),
                        appointment.getUser().getId(),
                        appointment.getAstrologer().getId()
                )
        );
        return  saved;
    }

    private static int getBookingLimit(Optional<BookingConfig> configOpt, LocalDate appointmentDate) {
        int bookingLimit = 25; // default
        if (configOpt.isPresent()) {
            BookingConfig config = configOpt.get();

            // custom booking limit if set
            if (config.getBookingLimit() != null) {
                bookingLimit = config.getBookingLimit();
            }

            // 3. Check not-available date range
            if (config.getNotAvailableStartDate() != null && config.getNotAvailableEndDate() != null) {
                LocalDate start = config.getNotAvailableStartDate();
                LocalDate end = config.getNotAvailableEndDate();

                // if requested date is within not-available range
                if (!appointmentDate.isBefore(start) && !appointmentDate.isAfter(end)) {
                    throw new CustomException(HttpStatus.BAD_REQUEST, "Astrologer is not available on this date");
                }
            }

            // If you later want to also use notAvailableStartTime / notAvailableEndTime,
            // you can add appointment time and check here.
        }
        return bookingLimit;
    }

    private double calculateTotalCost(AstrologerDetails astrologer, SessionType sessionType, int duration) {
        switch (sessionType) {
            case CHAT -> {
                return duration * astrologer.getPricePerMinuteChat();
            }
            case AUDIO -> {
                return duration * astrologer.getPricePerMinuteVoice();
            }
            case VIDEO -> {
                return duration * astrologer.getPricePerMinuteVideo();
            }
            default -> {
                return 0.0;
            }
        }
    }

    public Page<BookingAppointmentDto> getAllBookedAppointment(UUID userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.Direction.DESC, "createdAt");
        Page<BookingAppointment> appointmentPage = bookingAppointmentRepository.findByAstrologer_IdOrUser_Id(userId,
                userId, pageable);
        return appointmentPage.map(BookingAppointmentDto::new);
    }

    public BookingAppointmentDto getAppointmentById(UUID id) {
        BookingAppointment appointment = bookingAppointmentRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Appointment not found"));
        return new BookingAppointmentDto(appointment);
    }

    @Transactional
    public BookingAppointmentDto updateStatus(UUID id, BookingStatus status, Integer otp) {
        BookingAppointment appointment = bookingAppointmentRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Appointment not found"));
        if(status == BookingStatus.COMPLETED) {
            if (otp== null || appointment.getOtp() != otp)
                throw new CustomException("Invalid otp");
        }
        appointment.setStatus(status);
        BookingAppointment saved = bookingAppointmentRepository.save(appointment);

        if(status==BookingStatus.CANCELLED) {
            eventPublisher.publishEvent(
                    new BookingCancelledEvent(
                            appointment.getId(),
                            appointment.getUser().getId()
                    )
            );
        } else if (status == BookingStatus.APPROVED) {
            eventPublisher.publishEvent(
                    new BookingApprovedEvent(
                            appointment.getId(),
                            appointment.getUser().getId()
                    )
            );
        }
        if (status == BookingStatus.APPROVED) return createChatSession(appointment.getId(), appointment.getSessionType());
        return new BookingAppointmentDto(saved);
    }

    public BookingAppointmentDto createChatSession(UUID appointmentId, SessionType type) {
        BookingAppointment appointment = bookingAppointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Appointment not found"));
        if(type == SessionType.CHAT) {
            ChatSession chatSession = ChatSession.builder()
                    .astrologer(appointment.getAstrologer())
                    .user(appointment.getUser())
                    .startedAt(LocalDateTime.now())
                    .status(ChatStatus.ACTIVE)
                    .appointment(appointment)
                    .totalMinutes(appointment.getAppointmentDuration())
                    .totalCost(appointment.getTotalCost())
                    .build();

            appointment.setChatSession(chatSession);
            BookingAppointment savedAppointment = bookingAppointmentRepository.save(appointment);
            eventPublisher.publishEvent(
                    new SessionCreatedEvent(
                            appointment.getUser().getId(),
                            appointment.getAstrologer().getId(),
                            chatSession.getId(),
                            SessionType.CHAT
                    )
            );
            return new BookingAppointmentDto(savedAppointment);
        } else {
            CallSession callSession = CallSession.builder()
                    .astrologer(appointment.getAstrologer())
                    .user(appointment.getUser())
                    .startedAt(LocalDateTime.now())
                    .sessionType(type)
                    .status(ChatStatus.ACTIVE)
                    .appointment(appointment)
                    .totalMinutes(appointment.getAppointmentDuration())
                    .totalCost(appointment.getTotalCost())
                    .build();
            appointment.setCallSession(callSession);

            BookingAppointment savedAppointment = bookingAppointmentRepository.save(appointment);

            eventPublisher.publishEvent(
                    new SessionCreatedEvent(
                            appointment.getUser().getId(),
                            appointment.getAstrologer().getId(),
                            callSession.getId(),
                            type
                    )
            );
            return new BookingAppointmentDto(savedAppointment);
        }
    }
}
