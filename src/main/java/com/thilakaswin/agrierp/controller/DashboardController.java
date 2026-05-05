package com.thilakaswin.agrierp.controller;

import com.thilakaswin.agrierp.dto.DashboardSummaryDTO;
import com.thilakaswin.agrierp.dto.TaskDTO;
import com.thilakaswin.agrierp.dto.TaskCompletionDTO;
import com.thilakaswin.agrierp.service.DashboardService;
import com.thilakaswin.agrierp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TaskService taskService;


    @GetMapping("/summary")
    public DashboardSummaryDTO getSummary() {
        return dashboardService.getFarmSummary();
    }

    @GetMapping("/tasks")
    public List<TaskDTO> getDailyTasks() {
        return taskService.getDailyTasks();
    }

    // Update this specific endpoint to accept a RequestBody
    @PostMapping("/tasks/{taskId}/complete")
    public void completeTask(@PathVariable String taskId, @RequestBody(required = false) TaskCompletionDTO dto) {
        taskService.completeTask(taskId, dto);
    }
}