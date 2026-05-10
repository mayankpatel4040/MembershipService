package com.firstclub.membership.controller;

import com.firstclub.membership.dto.MembershipPlanDetailResponse;
import com.firstclub.membership.dto.UserMembershipDetail;
import com.firstclub.membership.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
@Validated
@Tag(name = "Membership", description = "Plans, subscriptions, tier transitions, and member details")
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/plans")
    @Operation(summary = "List active plans for a user",
            description = "Returns every active membership plan along with the user's current tier and tier benefits.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plans returned"),
            @ApiResponse(responseCode = "404", description = "User or plans not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<MembershipPlanDetailResponse> getMembershipPlans(
            @RequestParam("phone_number") @NotBlank String phoneNumber) {
        return ResponseEntity.ok(membershipService.getMembershipPlans(phoneNumber));
    }

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe a user to a plan",
            description = "Creates a new active subscription. Fails if the user already has one — use /changePlan instead.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription created"),
            @ApiResponse(responseCode = "404", description = "User or plan not found"),
            @ApiResponse(responseCode = "409", description = "User already has an active subscription")
    })
    public ResponseEntity<String> subscribeToMembership(
            @RequestParam("phone_number") @NotBlank String phoneNumber,
            @RequestParam("plan_name") @NotBlank String planName) {
        return ResponseEntity.ok(membershipService.subscribeToPlan(phoneNumber, planName));
    }

    @PatchMapping("/changePlan")
    @Operation(summary = "Upgrade or downgrade an active subscription",
            description = "Closes the current subscription and opens a new one in a single transaction. Direction is inferred from plan price.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan changed"),
            @ApiResponse(responseCode = "404", description = "User or plan not found"),
            @ApiResponse(responseCode = "409", description = "User already on the requested plan")
    })
    public ResponseEntity<String> changePlan(
            @RequestParam("phone_number") @NotBlank String phoneNumber,
            @RequestParam("new_plan_name") @NotBlank String newPlanName) {
        return ResponseEntity.ok(membershipService.changePlan(phoneNumber, newPlanName));
    }

    @PatchMapping("/unsubscribe")
    @Operation(summary = "Cancel an active subscription",
            description = "Marks the user's active subscription as CANCELLED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription cancelled"),
            @ApiResponse(responseCode = "404", description = "User or active subscription not found")
    })
    public ResponseEntity<String> unsubscribeFromMembership(
            @RequestParam("phone_number") @NotBlank String phoneNumber) {
        return ResponseEntity.ok(membershipService.cancelSubscription(phoneNumber));
    }

    @GetMapping("/userMembershipDetails")
    @Operation(summary = "Get the user's active subscription",
            description = "Returns plan name, start/end dates, and the user's current tier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership details returned"),
            @ApiResponse(responseCode = "404", description = "User or active subscription not found")
    })
    public ResponseEntity<UserMembershipDetail> getUserMembershipDetails(
            @RequestParam("phone_number") @NotBlank String phoneNumber) {
        return ResponseEntity.ok(membershipService.getUserMembershipDetails(phoneNumber));
    }
}
