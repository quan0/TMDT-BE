package com.mindwell.be.repository;

import com.mindwell.be.entity.MeetingPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingPlatformRepository extends JpaRepository<MeetingPlatform, Integer> {

	Optional<MeetingPlatform> findFirstByIsActiveTrueOrderByPlatformIdAsc();
}
