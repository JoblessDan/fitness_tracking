package com.example.fitness_tracking.service;

import com.example.fitness_tracking.model.User;
import com.example.fitness_tracking.model.Workout;
import com.example.fitness_tracking.repository.WorkoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkoutAnalyticsService {
    @Autowired
    private WorkoutRepository workoutRepository;

    /**
     * Generates a comprehensive set of analytics for a user's workouts.
     * This method calculates and returns metrics related to the user's workout activity,
     * which can be used for tracking performance, understanding workout habits,
     * and enabling machine learning-related insights.
     *
     * @param user The user for whom the workout analytics are to be generated.
     * @return A map containing various workout analytics, including:
     *         - "totalWorkouts": The total count of workouts completed by the user.
     *         - "averageCaloriesBurned": The average number of calories burned across all workouts.
     *         - "workoutTypeDistribution": A mapping of workout types to their respective counts.
     *         - "performanceTrends": A map detailing trends in workout performance, such as weekly averages,
     *           progression by workout type, and intensity trends.
     */
    public Map<String, Object> generateWorkoutAnalytics(User user) {
        Map<String, Object> analytics = new HashMap<>();

        // Calculate metricsfor ML
        analytics.put("totalWorkouts", workoutRepository.countByUser(user));
        analytics.put("averageCaloriesBurned", calculateAverageCalories(user));
        analytics.put("workoutTypeDistribution", getWorkoutTypeDistribution(user));
        analytics.put("performanceTrends", calculatePerformanceTrends(user));

        return analytics;
    }

    /**
     * Calculates the average number of calories burned across all workouts for a given user.
     * If the user has no workouts, the method returns 0.0.
     *
     * @param user The user whose workout data is being analyzed.
     * @return The average calories burned across all workouts for the specified user,
     *         or 0.0 if there are no workouts associated with the user.
     */
    private Double calculateAverageCalories(User user) {
        List<Workout> workouts = workoutRepository.findByUser(user);
        if (workouts.isEmpty()) {
            return 0.0;
        }
        return workouts.stream()
            .mapToDouble(Workout::getCaloriesBurned)
            .average()
            .orElse(0.0);
    }

    /**
     * Calculates the distribution of workout types for a given user.
     * The method retrieves all workouts associated with the user and groups them by workout type,
     * providing a count for each type.
     *
     * @param user The user whose workout type distribution is to be calculated.
     * @return A map where the keys are workout types (as strings) and the values are the counts of workouts of each type.
     */
    private Map<String, Integer> getWorkoutTypeDistribution(User user) {
        List<Workout> workouts = workoutRepository.findByUser(user);
        return workouts.stream()
            .collect(Collectors.groupingBy(
                Workout::getWorkoutType,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
    }

    /**
     * Calculates performance trends based on a user's workouts. This method aggregates
     * and analyzes workout data to identify meaningful trends that can be used to
     * track and improve the user's fitness journey. The resulting trends include
     * weekly averages, progression by workout type, and overall intensity trends.
     *
     * Key trends computed by this method:
     * - Weekly Averages: Metrics such as average calories burned, average duration,
     *   and average heart rate grouped by week.
     * - Progression by Type: Changes in workout intensity over time for each workout type.
     * - Intensity Trends: Metrics highlighting changes in workout intensity and heart rate
     *   variability across all workouts.
     *
     * @param user The user for whom performance trends are to be calculated.
     * @return A map containing calculated trends, structured as follows:
     *         - "weeklyAverages": A map of weekly metrics (e.g., calories, duration, heart rate averages) grouped by week.
     *         - "progressionByType": A map of workout types to their respective intensity progression over time.
     *         - "intensityTrends": A map indicating overall intensity metrics such as average intensity and heart rate variability.
     */
    private Map<String, Object> calculatePerformanceTrends(User user) {
        List<Workout> workouts = workoutRepository.findByUser(user);
        Map<String, Object> trends = new HashMap<>();

      
        Map<Integer, List<Workout>> workoutsByWeek = workouts.stream()
            .collect(Collectors.groupingBy(w ->
                w.getTimestamp().get(WeekFields.ISO.weekOfWeekBasedYear())));

        // Calculate various trends
        trends.put("weeklyAverages", calculateWeeklyAverages(workoutsByWeek));
        trends.put("progressionByType", calculateProgressionByType(workouts));
        trends.put("intensityTrends", calculateIntensityTrends(workouts));

        return trends;
    }

    /**
     * Calculates weekly averages for workout metrics such as calories burned, duration, and heart rate.
     * This method takes a mapping of workouts grouped by week and computes the average values for each
     * metric per week.
     *
     * @param workoutsByWeek A map where the keys represent week numbers (as integers) and the values
     *                       are lists of {@code Workout} objects corresponding to that week.
     * @return A map where the keys are week numbers (as integers) and the values are maps containing
     *         the average metrics for that week. These metrics include:
     *         - "avgCalories": The average number of calories burned.
     *         - "avgDuration": The average workout duration in minutes.
     *         - "avgHeartRate": The average heart rate during workouts.
     */
    private Map<Integer, Map<String, Double>> calculateWeeklyAverages(
            Map<Integer, List<Workout>> workoutsByWeek) {
        Map<Integer, Map<String, Double>> weeklyAverages = new HashMap<>();

        workoutsByWeek.forEach((week, weekWorkouts) -> {
            Map<String, Double> averages = new HashMap<>();
            averages.put("avgCalories", weekWorkouts.stream()
                .mapToDouble(Workout::getCaloriesBurned)
                .average()
                .orElse(0.0));
            averages.put("avgDuration", weekWorkouts.stream()
                .mapToDouble(Workout::getDurationMinutes)
                .average()
                .orElse(0.0));
            averages.put("avgHeartRate", weekWorkouts.stream()
                .mapToDouble(Workout::getAverageHeartRate)
                .average()
                .orElse(0.0));

            weeklyAverages.put(week, averages);
        });

        return weeklyAverages;
    }

    /**
     * Calculates the progression of workout intensities grouped by workout type.
     * This method analyzes a list of workouts and organizes them by their type, calculating
     * the intensity for each workout as calories burned per minute and sorting them in
     * chronological order. The result is a mapping of workout types to lists of intensity
     * values over time, which provides insight into how workout performance is changing
     * for each type.
     *
     * The calculation for intensity is determined as:
     * {@code intensity = caloriesBurned / durationMinutes}.
     *
     * @param workouts A list of {@code Workout} objects representing individual workout sessions.
     *                 These must include details such as the workout type, timestamp, calories burned,
     *                 and duration in minutes.
     * @return A map where:
     *         - The keys are strings representing workout types (e.g., "running", "cycling").
     *         - The values are lists of doubles representing the intensities of the workouts,
     *           sorted by time in ascending order.
     */
    private Map<String, List<Double>> calculateProgressionByType(List<Workout> workouts) {
        Map<String, List<Double>> progression = new HashMap<>();

        // Group workouts by type and calculate progression
        Map<String, List<Workout>> byType = workouts.stream()
            .collect(Collectors.groupingBy(Workout::getWorkoutType));

        byType.forEach((type, typeWorkouts) -> {
            List<Double> intensities = typeWorkouts.stream()
                .sorted(Comparator.comparing(Workout::getTimestamp))
                .map(w -> (w.getCaloriesBurned() / w.getDurationMinutes()))
                .collect(Collectors.toList());
            progression.put(type, intensities);
        });

        return progression;
    }

    /**
     * Calculates overall intensity trends for a list of workouts.
     * This method computes metrics that provide insights into the overall intensity
     * of workout sessions and their variability in terms of heart rate.
     *
     * Intensity trends include:
     * - "averageIntensity": The average intensity across all workouts, calculated
     *   as calories burned per minute of workout.
     * - "averageHeartRateVariability": The average heart rate variability across all
     *   workouts, calculated as the difference between the maximum heart rate and
     *   the resting heart rate.
     *
     * If no workouts are provided, the method defaults both metrics to 0.0.
     *
     * @param workouts A list of {@code Workout} objects representing different workout sessions.
     *                 Each {@code Workout} must include information such as calories burned,
     *                 duration in minutes, maximum heart rate, and resting heart rate.
     * @return A map containing the calculated intensity trends where:
     *         - The key "averageIntensity" maps to the average workout intensity.
     *         - The key "averageHeartRateVariability" maps to the heart rate variability.
     */
    private Map<String, Double> calculateIntensityTrends(List<Workout> workouts) {
        Map<String, Double> intensityTrends = new HashMap<>();

        // Calculate average intensity metrics
        double avgIntensity = workouts.stream()
            .mapToDouble(w -> w.getCaloriesBurned() / w.getDurationMinutes())
            .average()
            .orElse(0.0);

        double avgHeartRateVariability = workouts.stream()
            .mapToDouble(w -> w.getMaxHeartRate() - w.getRestingHeartRate())
            .average()
            .orElse(0.0);

        intensityTrends.put("averageIntensity", avgIntensity);
        intensityTrends.put("averageHeartRateVariability", avgHeartRateVariability);

        return intensityTrends;
    }

    /**
     * Prepares data for machine learning by extracting relevant features from a user's workout history.
     * This method processes the workouts associated with the given user and generates a list of feature
     * vectors, where each vector corresponds to a workout session and contains numerical values representing
     * key metrics of the workout.
     *
     * The generated feature vector for each workout includes the following attributes:
     * - Duration in minutes.
     * - Average heart rate during the workout.
     * - Maximum heart rate during the workout.
     * - Resting heart rate.
     * - Ambient temperature during the workout.
     * - Hours of sleep prior to the workout.
     * - Stress level during the workout.
     *
     * @param user The user whose workouts will be processed to generate machine learning data.
     * @return A list of double arrays, where each array represents the feature vector for a workout session.
     *         Each array is of fixed length and contains the metrics listed above in the specified order.
     */
    public List<double[]> prepareDataForML(User user) {
        List<Workout> workouts = workoutRepository.findByUser(user);
        List<double[]> mlData = new ArrayList<>();

        for (Workout workout : workouts) {
            // Create feature vectors for ML
            double[] features = {
                workout.getDurationMinutes(),
                workout.getAverageHeartRate(),
                workout.getMaxHeartRate(),
                workout.getRestingHeartRate(),
                workout.getTemperature(),
                workout.getHoursSlept(),
                workout.getStressLevel()
            };
            mlData.add(features);
        }

        return mlData;
    }
}