package crm.web.controller;

import crm.model.enums.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Exposes enum option lists so the front-end can render dropdowns without
 * hard-coding domain values.
 */
@RestController
@RequestMapping("/api/meta")
public class MetaController {

    public record Option(String name, String label) {}

    @GetMapping("/profile-areas")
    public List<Option> profileAreas() {
        return Arrays.stream(ProfileArea.values())
                .map(p -> new Option(p.name(), p.getLabel()))
                .toList();
    }

    @GetMapping("/experience-levels")
    public List<Option> experienceLevels() {
        return Arrays.stream(ExperienceLevel.values())
                .map(p -> new Option(p.name(), p.getLabel()))
                .toList();
    }

    @GetMapping("/lead-statuses")
    public List<Option> leadStatuses() {
        return Arrays.stream(LeadStatus.values())
                .map(p -> new Option(p.name(), p.getLabel()))
                .toList();
    }

    @GetMapping("/opportunity-stages")
    public List<Option> opportunityStages() {
        return Arrays.stream(OpportunityStage.values())
                .map(p -> new Option(p.name(), p.getLabel()))
                .toList();
    }

    @GetMapping("/all")
    public Map<String, List<Option>> all() {
        return Map.of(
                "profileAreas", profileAreas(),
                "experienceLevels", experienceLevels(),
                "leadStatuses", leadStatuses(),
                "opportunityStages", opportunityStages()
        );
    }
}
