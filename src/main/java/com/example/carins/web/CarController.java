package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.OwnerRepository;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.CarHistoryEventDto;
import com.example.carins.web.dto.ClaimDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;
    private final InsuranceClaimRepository claimRepo;
    private final CarRepository carRepo;
    private final OwnerRepository ownerRepo;

    public CarController(CarService service, InsuranceClaimRepository claimRepo, CarRepository carRepo, OwnerRepository ownerRepo) {
        this.service = service;
        this.claimRepo = claimRepo;
        this.carRepo = carRepo;
        this.ownerRepo = ownerRepo;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }

    @PostMapping("/cars")
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarDto carDto) {

        var owner = ownerRepo.findById(carDto.ownerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Owner not found"
                ));

        Car car = new Car(
                carDto.vin(),
                carDto.make(),
                carDto.model(),
                carDto.year(),
                owner
        );

        Car saved = service.createCar(car);
        return ResponseEntity
                .created(URI.create("/api/cars/" + saved.getId()))
                .body(toDto(saved));
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@PathVariable Long carId, @RequestParam String date) {
        boolean valid = service.isInsuranceValid(carId, date);
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, date, valid));
    }

    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<?> registerClaim(@PathVariable Long carId,
                                           @Valid @RequestBody ClaimRequest request) {
        var car = carRepo.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Car with id " + carId + " not found"));

        InsuranceClaim claim = new InsuranceClaim(
                car,
                request.claimDate(),
                request.description(),
                request.amount()
        );
        InsuranceClaim saved = claimRepo.save(claim);

        URI location = URI.create("/api/cars/" + carId + "/claims/" + saved.getId());
        return ResponseEntity.created(location).body(toClaimDto(saved));
    }

    @GetMapping("/cars/{carId}/history")
    public ResponseEntity<List<CarHistoryEventDto>> getCarHistory(@PathVariable Long carId) {
        List<CarHistoryEventDto> history = service.getCarHistory(carId);
        return ResponseEntity.ok(history);
    }

    private ClaimDto toClaimDto(InsuranceClaim claim) {
        return new ClaimDto(claim.getId(),
                claim.getCar().getId(),
                claim.getClaimDate(),
                claim.getDescription(),
                claim.getAmount());
    }

    public record ClaimRequest(
            @NotNull
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate claimDate,
            @NotBlank String description,
            @NotNull @DecimalMin("0.01") BigDecimal amount
    ) {}

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
}
