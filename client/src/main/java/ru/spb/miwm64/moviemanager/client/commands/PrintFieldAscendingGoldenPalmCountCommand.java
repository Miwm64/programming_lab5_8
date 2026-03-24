package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.client.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.client.command.*;
import ru.spb.miwm64.moviemanager.common.entities.Movie;

import java.util.List;

public final class PrintFieldAscendingGoldenPalmCountCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public PrintFieldAscendingGoldenPalmCountCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.name = "print_field_ascending_golden_palm_count";
        this.help = "print_field_ascending_golden_palm_count - displays all goldenPalmCount values in ascending order";

        var countParam = new Parameter<Long>(
                "goldenPalmCount",
                "Enter golden palm count",
                Long::parseLong,
                l -> l > 0,
                true
        );
    }

    @Override
    public CommandResult execute() {
        try {
            checkParams();

            List<Movie> movies = collectionManager.getAll();

            if (movies.isEmpty()) {
                return new CommandResultSuccess(
                        List.of(),
                        "Collection is empty"
                );
            }

            List<Movie> sortedMovies = collectionManager.printFieldAscendingGoldenPalmCountCommand();

            StringBuilder message = new StringBuilder();
            message.append("Golden palm counts (ascending):\n");

            for (Movie movie : sortedMovies) {
                message.append(String.format("  ID: %-4d | Golden palms: %d\n",
                        movie.getId(),
                        movie.getGoldenPalmCount()));
            }

            return new CommandResultSuccess(sortedMovies, message.toString());

        } catch (Exception e) {
            return new CommandResultFailure("Failed: " + e.getMessage());
        }
    }
}