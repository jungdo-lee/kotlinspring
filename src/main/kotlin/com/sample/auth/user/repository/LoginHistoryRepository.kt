package com.sample.auth.user.repository

import com.sample.auth.user.entity.LoginHistory
import org.springframework.data.jpa.repository.JpaRepository

interface LoginHistoryRepository : JpaRepository<LoginHistory, Long>
