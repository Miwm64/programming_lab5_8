package ru.spb.miwm64.moviemanager.commands;

import ru.spb.miwm64.moviemanager.collectionmanager.CollectionManager;
import ru.spb.miwm64.moviemanager.command.*;
import ru.spb.miwm64.moviemanager.entities.*;

import java.time.ZonedDateTime;

public final class UpdateByIDCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public UpdateByIDCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;

        this.name = "update_id";
        this.help = "update_id <id> - updates element by speicified id";

        // id, can not be null, >0
        Parameter<Long> idParam = new Parameter<>(
                "id",
                "Enter id",
                Long::parseLong,
                s -> s > 0,
                true
        );

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
                s -> MovieGenre.valueOf(s.toUpperCase()),
                genre -> true,
                false
        );

        // mpaaRating - MpaaRating enum, cannot be null
        Parameter<MpaaRating> mpaaParam = new Parameter<>(
                "mpaaRating",
                "Enter mpaa rating (PG_13/R/NC_17)",
                s -> MpaaRating.valueOf(s.toUpperCase().replace("-", "")),
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
                s -> Color.valueOf(s.toUpperCase()),
                color -> true,
                false
        );

        // operator.nationality - Country enum, cannot be null
        Parameter<Country> nationalityParam = new Parameter<>(
                "nationality",
                "Enter nationality (UNITED_KINGDOM/CHINA/INDIA/ITALY/THAILAND)",
                s -> Country.valueOf(s.toUpperCase()),
                country -> true,
                false
        );

        // Movie fields
        addParam(idParam);
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
                    getValue("id"),
                    getValue("name"),
                    coords,
                    ZonedDateTime.now(),
                    getValue("oscarsCount"),
                    getValue("goldenPalmCount"),
                    MovieGenre.DRAMA,
                    MpaaRating.PG_13,
                    operator
            );

            collectionManager.setById(getValue("id"), movie);

            return new CommandResultSuccess(
                    movie,
                    "Movie updated successfully by ID: " + movie.getId()
            );

        } catch (Exception e) {
            return new CommandResultFailure("Failed to get collection info: " + e.getMessage());
        }
    }
}
