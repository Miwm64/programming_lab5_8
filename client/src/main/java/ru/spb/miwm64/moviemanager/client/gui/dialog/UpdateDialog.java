package ru.spb.miwm64.moviemanager.client.gui.dialog;

import javafx.scene.control.TextField;
import ru.spb.miwm64.moviemanager.client.command.Parameter;
import ru.spb.miwm64.moviemanager.client.commands.AddCommand;
import ru.spb.miwm64.moviemanager.common.entities.*;

public final class UpdateDialog extends MyDialog {
    public UpdateDialog(Movie movie) {
        super();
        titleLabel.setText("Update Movie");
        parameterList = new AddCommand(null).getParams();

        for (var param : parameterList) {
            addField(param);
        }
        setFields(movie);
    }

    private void setFields(Movie movie) {
        for (Parameter<?> param : parameterList) {
            String paramName = param.getName();
            TextField field = fieldMap.get(paramName);
            if (field == null) continue;

            Object value = getMovieFieldValue(movie, paramName);
            field.setText(value != null ? value.toString() : "");
        }
    }

    private Object getMovieFieldValue(Movie movie, String paramName) {
        switch (paramName) {
            case "name":
                return movie.getName();
            case "coordX":
                return movie.getCoordinates() != null ? movie.getCoordinates().getX() : null;
            case "coordY":
                return movie.getCoordinates() != null ? movie.getCoordinates().getY() : null;
            case "oscarsCount":
                return movie.getOscarsCount();
            case "goldenPalmCount":
                return movie.getGoldenPalmCount();
            case "genre":
                return movie.getGenre();
            case "mpaaRating":
                return movie.getMpaaRating();
            case "operatorName":
                return movie.getOperator() != null ? movie.getOperator().getName() : null;
            case "operatorWeight":
                return movie.getOperator() != null ? movie.getOperator().getWeight() : null;
            case "hairColor":
                return movie.getOperator() != null ? movie.getOperator().getHairColor() : null;
            case "nationality":
                return movie.getOperator() != null ? movie.getOperator().getNationality() : null;
            default:
                return null;
        }
    }
}