package com.shop.userservice.repository;

import com.shop.userservice.entity.User;
import com.shop.userservice.util.NormalizerPhoneNumber;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class UserSpecification {
    public static Specification<User> hasFirstName(String firstName) {
        return (root, query, cb) ->
                firstName == null ? null : cb.like(cb.lower(root.get("firstName")), "%" +
                        firstName.toLowerCase() + "%");
    }

    public static Specification<User> hasPatronymic(String patronymic) {
        return (root, query, cb) ->
                patronymic == null ? null : cb.like(cb.lower(root.get("patronymic")), "%" +
                        patronymic.toLowerCase() + "%");
    }

    public static Specification<User> hasLastName(String lastName) {
        return (root, query, cb) ->
                lastName == null ? null : cb.equal(cb.lower(root.get("lastName")), lastName.toLowerCase());
    }

    public static Specification<User> hasPhoneNumber(String phoneNumber) {
        return (root, query, cb) ->
                phoneNumber == null ? null : cb.equal(root.get("phoneNumber"), NormalizerPhoneNumber.normalizerPhone(phoneNumber));
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) ->
                email == null ? null : cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<User> createdBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;

            if (from != null && to != null) {
                OffsetDateTime fromConvert = OffsetDateTime.of(from, LocalTime.MIN, ZoneOffset.UTC);
                OffsetDateTime toConvert = OffsetDateTime.of(to, LocalTime.MAX, ZoneOffset.UTC);

                return cb.between(root.get("createdAt"), fromConvert, toConvert);
            } else if (from != null) {
                OffsetDateTime fromConvert = OffsetDateTime.of(from, LocalTime.MIN, ZoneOffset.UTC);

                return cb.greaterThanOrEqualTo(root.get("createdAt"), fromConvert);
            } else {
                OffsetDateTime toConvert = OffsetDateTime.of(to, LocalTime.MAX, ZoneOffset.UTC);

                return cb.lessThanOrEqualTo(root.get("createdAt"), toConvert);
            }
        };
    }
}
