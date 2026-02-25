package ru.spb.miwm64.moviemanager.commands;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.command.*;
import ru.spb.miwm64.moviemanager.entities.Color;
import ru.spb.miwm64.moviemanager.entities.Country;
import ru.spb.miwm64.moviemanager.entities.Movie;
import ru.spb.miwm64.moviemanager.entities.Person;

public final class FilterGreaterThanOperatorCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public FilterGreaterThanOperatorCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.name = "filter_greater_than_operator";
        this.help = "filter_greater_than_operator <name> - displays elements whose " +
                "operator is greater than the specified value";

        // operator.name - String, cannot be null or empty
        Parameter<String> operatorNameParam = new Parameter<>(
                "operatorName",
                "Enter operator name",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                true
        );

        // operator.weight - float, > 0
        Parameter<Float> operatorWeightParam = new Parameter<>(
                "operatorWeight",
                "Enter operator weight (float > 0)",
                Float::parseFloat,
                w -> w > 0,
                true
        );

        // operator.hairColor - Color enum, cannot be null
        Parameter<Color> hairColorParam = new Parameter<>(
                "hairColor",
                "Enter hair color (GREEN/RED/YELLOW/ORANGE/BROWN)",
                s -> Color.valueOf(s.toUpperCase()),
                color -> true,
                true
        );

        // operator.nationality - Country enum, cannot be null
        Parameter<Country> nationalityParam = new Parameter<>(
                "nationality",
                "Enter nationality (UNITED_KINGDOM/CHINA/INDIA/ITALY/THAILAND)",
                s -> Country.valueOf(s.toUpperCase()),
                country -> true,
                true
        );

        addParam(operatorNameParam);
        addParam(operatorWeightParam);
        addParam(hairColorParam);
        addParam(nationalityParam);
    }

    @Override
    public CommandResult execute() {
        try {
            checkParams();

            // Build reference person from ALL input parameters
            String targetName = getValue("operatorName");
            Float targetWeight = getValue("operatorWeight");
            Color targetHairColor = getValue("hairColor");
            Country targetNationality = getValue("nationality");

            Person referencePerson = new Person(
                    targetName,
                    targetWeight,
                    targetHairColor,
                    targetNationality
            );

            var filteredMovies = collectionManager.getAll().stream()
                    .filter(movie -> movie.getOperator() != null)
                    .filter(movie -> movie.getOperator().compareTo(referencePerson) > 0)
                    .toList();

            if (filteredMovies.isEmpty()) {
                return new CommandResultSuccess(
                        filteredMovies,
                        "No movies found with operator greater than:\n" +
                                "  Name: " + targetName + "\n" +
                                "  Weight: " + targetWeight + "\n" +
                                "  Hair Color: " + targetHairColor + "\n" +
                                "  Nationality: " + targetNationality
                );
            }

            StringBuilder message = new StringBuilder();
            message.append(String.format("Movies with operator greater than reference (%d found):\n",
                    filteredMovies.size()));
            message.append("Reference operator:\n");
            message.append(String.format("  Name: %s, Weight: %.1f, Hair: %s, Nationality: %s\n",
                    targetName, targetWeight, targetHairColor, targetNationality));
            message.append("Found movies:\n");

            for (Movie movie : filteredMovies) {
                Person op = movie.getOperator();
                message.append(String.format("  Movie: %s, Operator: %s (Weight: %.1f, Hair: %s, Nationality: %s)\n",
                        movie.getName(),
                        op.getName(),
                        op.getWeight(),
                        op.getHairColor(),
                        op.getNationality()));
            }

            return new CommandResultSuccess(filteredMovies, message.toString());

        } catch (Exception e) {
            return new CommandResultFailure("Failed to filter: " + e.getMessage());
        }
    }
}