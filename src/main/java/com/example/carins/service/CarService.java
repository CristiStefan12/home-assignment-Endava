package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.CarHistoryEventDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final InsuranceClaimRepository claimRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, InsuranceClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public Car createCar(Car car) {
        if (carRepository.findByVin(car.getVin()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Car with VIN " + car.getVin() + " already exists"
            );
        }
        return carRepository.save(car);
    }

    public boolean isInsuranceValid(Long carId, String dateStr) {
        if (carId == null || dateStr == null || dateStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "carId and date must not be null or empty");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Expected ISO format: YYYY-MM-DD");
        }

        if (date.isBefore(LocalDate.of(1900, 1, 1)) || date.isAfter(LocalDate.of(2100, 12, 31))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is outside supported range (1900–2100)");
        }

        if (!carRepository.existsById(carId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car with id " + carId + " not found");
        }

        return policyRepository.existsActiveOnDate(carId, date);
    }


    public List<CarHistoryEventDto> getCarHistory(Long carId) {
        if(!carRepository.existsById(carId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car with id " + carId + " not found");
        }

        List<CarHistoryEventDto> events = new ArrayList<>();

        policyRepository.findByCarId(carId).forEach(p -> {
            String provider = p.getProvider() != null ? p.getProvider() : "N/A";
            String desc = "Policy from " + p.getStartDate() + " to " + p.getEndDate() +
                    " with provider " + provider;
            events.add(new CarHistoryEventDto("POLICY", p.getStartDate(), desc));
        });

        claimRepository.findByCarId(carId).forEach(c -> {
            String desc = c.getDescription() + " (amount " + c.getAmount() + ")";
            events.add(new CarHistoryEventDto("CLAIM", c.getClaimDate(), desc));
        });

        events.sort(Comparator.comparing(CarHistoryEventDto::getDate));

        return events;
    }
}
