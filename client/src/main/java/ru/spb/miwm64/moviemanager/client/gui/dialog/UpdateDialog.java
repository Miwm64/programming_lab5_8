package ru.spb.miwm64.moviemanager.client.gui.dialog;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import ru.spb.miwm64.moviemanager.client.command.Command;
import ru.spb.miwm64.moviemanager.client.command.Parameter;
import ru.spb.miwm64.moviemanager.client.commands.AddCommand;
import ru.spb.miwm64.moviemanager.client.commands.UpdateByIDCommand;
import ru.spb.miwm64.moviemanager.client.gui.util.GuiFactory;
import ru.spb.miwm64.moviemanager.client.gui.util.I18N;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.*;

import java.time.LocalDate;
import java.time.ZoneId;

public final class UpdateDialog extends MyDialog {
    private Movie movie;
    private final CollectionManager collectionManager;
    public UpdateDialog(Movie movie, CollectionManager collectionManager) {
        super();
        this.collectionManager = collectionManager;
        titleLabel.setText(I18N.get("update_dialog.title"));
        parameterList = new AddCommand(null).getParams();

        for (var param : parameterList) {
            addField(param);
        }
        setFields(movie);
    }

    private void setFields(Movie movie) {
        this.movie = movie;
        for (Parameter<?> param : parameterList) {
            String paramName = param.getName();
            Control control = fieldMap.get(paramName);
            if (control == null) continue;

            Object value = getMovieFieldValue(movie, paramName);
            if (control instanceof TextField) {
                TextField field = (TextField) control;
                if  (value == null) continue;
                field.setText(value != null ? value.toString() : "");
                param.fromString(field.getText());
            }
            else if (control instanceof ComboBox) {
                ComboBox<Object> combo = (ComboBox<Object>) control;
                if (value == null) continue;
                combo.setValue(value);
                param.fromString(combo.getValue().toString());
            }
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
    @Override
    protected void execute(){
        Platform.runLater(() -> {
            Command updateCommand = new UpdateByIDCommand(collectionManager);
            updateCommand.setParams(parameterList);
            Parameter<?> param = updateCommand.getRemainingRequiredParams().get(0);
            param.fromString(this.movie.getId() + "");
            updateCommand.setParam(param);
            var res = updateCommand.execute();
            if (!res.isSuccess()) {
                GuiFactory.createErrorPopupWithProperty("update_dialog.error.could_not_update").show();
            }
        });
    }
}