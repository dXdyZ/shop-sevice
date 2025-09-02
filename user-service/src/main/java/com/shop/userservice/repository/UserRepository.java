package com.shop.userservice.repository;

import com.shop.userservice.dto.UserSearchDto;
import com.shop.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUserUUID(UUID userUUID);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);

    Boolean existsByPhoneNumber(String phoneNumber);
    Boolean existsByEmail(String email);
    Boolean existsByUserUUID(UUID userUUID);


    default Page<User> searchUser(UserSearchDto request, Pageable pageable) {
        Specification<User> specification = Specification.unrestricted();

        if (request != null) {
            specification = specification.and(UserSpecification.hasFirstName(request.firstName()))
                    .and(UserSpecification.hasLastName(request.lastName()))
                    .and(UserSpecification.hasPatronymic(request.patronymic()))
                    .and(UserSpecification.hasPhoneNumber(request.phoneNumber()))
                    .and(UserSpecification.hasEmail(request.email()))
                    .and(UserSpecification.createdBetween(request.createdFrom(), request.createdTo()));
        }

        return findAll(specification, pageable);
    }
}
