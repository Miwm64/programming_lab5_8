package ru.spb.miwm64.moviemanager.client.command;

import ru.spb.miwm64.moviemanager.client.exceptions.NonExistentParameter;
import ru.spb.miwm64.moviemanager.client.exceptions.NotEnoughParameters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


abstract public class AbstractCommand implements  Command {
    protected Map<String, Parameter<?>> params = new LinkedHashMap<>();
    protected String name;
    protected String help;

    @Override
    public ArrayList<Parameter<?>> getParams(){
        var res = new ArrayList<Parameter<?>>();
        for (var param : params.values()){
            res.add(param.clone());
        }

        return res;
    }

    @Override
    public ArrayList<Parameter<?>> getMissingParams() {
        var res = new ArrayList<Parameter<?>>();
        for (var param : params.values()){
            if (!param.isSet()){
                res.add(param.clone());
            }
        }

        return res;
    }

    public ArrayList<Parameter<?>> getRemainingRequiredParams() {
        var res = new ArrayList<Parameter<?>>();
        for (var param : params.values()){
            if (!param.isSet() && param.isRequired()){
                res.add(param.clone());
            }
        }

        return res;
    }

    public void setParam(Parameter<?> param) {
        if (params.isEmpty()) {
            throw new NonExistentParameter("Params can not be set in command " + name);
        }
        if (!params.containsKey(param.getName())) {
            throw new NonExistentParameter(param.getName() + " can not be set in command " + name);
        }

        params.put(param.getName(), param);
    }

    public void setParams(ArrayList<Parameter<?>> params) {
        for (var param : params) {
            setParam(param);
        }
    }

    public String getHelp() {
        return help;
    }

    public String getName() {
        return name;
    }

    protected void addParam(Parameter<?> parameter) {
        params.put(parameter.getName(), parameter);
    }

    protected void checkParams() {
        int skip = 0;
        for (var param : params.values()){
            if (skip > 0){
                skip--;
                continue;
            }
            if (!param.isSet()){
                if (param.isComposite()){
                    skip = param.compositeSize()-1;
                    continue;
                }
                if (!param.isRequired()){
                    continue;
                }
                throw new NotEnoughParameters("Too few parameters were specified for " + name + " command");
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T getValue(String name) {
        return (T) params.get(name).getValue();
    }
}
