package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.client.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.client.command.*;

public final class CountByGoldenPalmCountCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public CountByGoldenPalmCountCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.name = "count_by_golden_palm_count";
        this.help = "count_by_golden_palm_count <value> - displays number of movies with specified golden palm count";

        var goldenPalmParam = new Parameter<Long>(
                "goldenPalmCount",
                "Enter golden palm count to search for",
                Long::parseLong,
                value -> value >= 0,
                true
        );
        addParam(goldenPalmParam);
    }

    @Override
    public CommandResult execute() {
        try {
            checkParams();
            Long targetCount = getValue("goldenPalmCount");

            long count = collectionManager.getAll().stream()
                    .filter(movie -> movie.getGoldenPalmCount() == targetCount)
                    .count();

            String message = String.format(
                    "Movies with golden palm count = %d: %d",
                    targetCount, count
            );

            return new CommandResultSuccess(count, message);

        } catch (Exception e) {
            return new CommandResultFailure("Failed to count: " + e.getMessage());
        }
    }
}