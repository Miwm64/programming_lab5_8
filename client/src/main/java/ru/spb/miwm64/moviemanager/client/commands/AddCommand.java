package ru.spb.miwm64.moviemanager.client.commands;

import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.client.command.*;
import ru.spb.miwm64.moviemanager.common.entities.*;

import java.time.ZonedDateTime;

public final class AddCommand extends AbstractCommand {
    private CollectionManager collectionManager;
    public AddCommand(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
        this.name = "add";
        this.help = "add - add new element to collection";


        // name - String, cannot be null or empty
        Parameter<String> nameParam = new Parameter<>(
                "name",
                "Enter movie name",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                true
        );

        // coordinates.x - float, max 274
        Parameter<Float> coordXParam = new Parameter<>(
                "coordX",
                "Enter coordinates x (float, max 274)",
                Float::parseFloat,
                x -> x <= 274,
                true
        );

        // coordinates.y - Long, > -559, cannot be null
        Parameter<Long> coordYParam = new Parameter<>(
                "coordY",
                "Enter coordinates y (Long, > -559)",
                Long::parseLong,
                y -> y > -559,
                true
        );


        // oscarsCount - int, > 0
        Parameter<Integer> oscarsParam = new Parameter<>(
                "oscarsCount",
                "Enter oscars count (int > 0)",
                Integer::parseInt,
                count -> count > 0,
                true
        );

        // goldenPalmCount - long, > 0
        Parameter<Long> goldenPalmParam = new Parameter<>(
                "goldenPalmCount",
                "Enter golden palm count (long > 0)",
                Long::parseLong,
                count -> count > 0,
                true
        );

        // genre - MovieGenre enum, can be null
        Parameter<MovieGenre> genreParam = new Parameter<>(
                "genre",
                "Enter genre (DRAMA/MUSICAL/TRAGEDY/THRILLER) or leave empty",
                s -> MovieGenre.fromString(s.toUpperCase()),
                genre -> true,
                false
        );

        // mpaaRating - MpaaRating enum, cannot be null
        Parameter<MpaaRating> mpaaParam = new Parameter<>(
                "mpaaRating",
                "Enter mpaa rating (PG_13/R/NC_17)",
                s -> MpaaRating.fromString(s.toUpperCase()),
                rating -> true,
                true
        );

        // operator.name - String, cannot be null or empty
        Parameter<String> operatorNameParam = new Parameter<>(
                "operatorName",
                "Enter operator name",
                s -> s,
                s -> s != null && !s.trim().isEmpty(),
                false
        );

        // operator.weight - float, > 0
        Parameter<Float> operatorWeightParam = new Parameter<>(
                "operatorWeight",
                "Enter operator weight (float > 0)",
                Float::parseFloat,
                w -> w > 0,
                false
        );

        // operator.hairColor - Color enum, cannot be null
        Parameter<Color> hairColorParam = new Parameter<>(
                "hairColor",
                "Enter hair color (GREEN/RED/YELLOW/ORANGE/BROWN)",
                s -> Color.fromString(s.toUpperCase()),
                color -> true,
                false
        );

        // operator.nationality - Country enum, cannot be null
        Parameter<Country> nationalityParam = new Parameter<>(
                "nationality",
                "Enter nationality (UNITED_KINGDOM/CHINA/INDIA/ITALY/THAILAND)",
                s -> Country.fromString(s.toUpperCase()),
                country -> true,
                false
        );

        // Movie fields
        addParam(nameParam);
        addParam(coordXParam);
        addParam(coordYParam);
        addParam(oscarsParam);
        addParam(goldenPalmParam);
        addParam(genreParam);
        addParam(mpaaParam);

        // Person fields (operator)
        addParam(operatorNameParam);
        addParam(operatorWeightParam);
        addParam(hairColorParam);
        addParam(nationalityParam);
    }

    @Override
    public CommandResult execute() {
        try {
            checkParams();

            Coordinates coords = new Coordinates(
                    getValue("coordX"),
                    getValue("coordY")
            );

            Person operator = null;
            if (params.get("operatorName").isSet() &&
                params.get("operatorWeight").isSet() &&
                params.get("hairColor").isSet() &&
                params.get("nationality").isSet() ) {
                operator = new Person(
                        getValue("operatorName"),
                        getValue("operatorWeight"),
                        getValue("hairColor"),
                        getValue("nationality")
                );
            }
            Movie movie = new Movie(
                    null,
                    getValue("name"),
                    coords,
                    ZonedDateTime.now(),
                    getValue("oscarsCount"),
                    getValue("goldenPalmCount"),
                    MovieGenre.DRAMA,
                    MpaaRating.PG_13,
                    operator
            );

            int createdMovieId = collectionManager.add(movie);

            return new CommandResultSuccess(
                    movie,
                    "Movie added successfully with ID: " + createdMovieId
            );
        } catch (Exception e) {
            return new CommandResultFailure(e.getMessage());
        }
    }
}
