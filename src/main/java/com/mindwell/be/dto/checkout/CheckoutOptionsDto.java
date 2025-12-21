package com.mindwell.be.dto.checkout;

import com.mindwell.be.dto.appointment.MeetingPlatformOptionDto;
import com.mindwell.be.dto.payment.PaymentMethodOptionDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "CheckoutOptions")
public record CheckoutOptionsDto(
        List<MeetingPlatformOptionDto> platforms,
        List<String> serviceTypes,
        List<PaymentMethodOptionDto> paymentMethods
) {
}
