package com.kss.astrologer.repository;

import java.util.Optional;
import java.util.UUID;

import com.kss.astrologer.types.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kss.astrologer.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByMobile(String mobile);

    Long countByRole(Role role);

    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.user.role = :role")
    Double getTotalWalletBalanceByRole(@Param("role") Role role);


}
