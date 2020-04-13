package de.jerosal.calculator;

import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.*;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalculatorAddon extends LabyModAddon {

    private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private ScriptEngine engine = scriptEngineManager.getEngineByName("JavaScript");
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private String commandName;

    @Override
    public void onEnable() {
        getApi().getEventManager().register(this::handleMessage);
    }

    private boolean handleMessage(String message) {
        if (message.startsWith(commandName)) {
            executorService.submit(() -> {
                String[] splitMessage = message.split(" ");
                try {
                    String[] args = Arrays.copyOfRange(splitMessage, 1, splitMessage.length);
                    Minecraft.getMinecraft().player.sendMessage(new TextComponentString(args.length == 1 ? calculate(args[0]) : "§cInvalid arguments."));
                } catch (ScriptException | IllegalArgumentException e) {
                    Minecraft.getMinecraft().player.sendMessage(new TextComponentString("§cThis is not a valid expression."));
                }
            });
            return true;
        }
        return false;
    }

    private void loadCommandPrefix() {
        if(!getConfig().has("command_name")) {
            getConfig().addProperty("command_name", "/calc");
        }
        commandName = getConfig().get("command_name").getAsString();
    }

    private String calculate(String input) throws ScriptException {
        return String.format("§aResult: §e%,.2f", Double.parseDouble(String.valueOf(engine.eval(input))));
    }

    @Override
    public void loadConfig() {
        loadCommandPrefix();
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        list.add(new StringElement("Command", this, new ControlElement.IconData(Material.DIODE), "command_name", commandName));
    }
}
