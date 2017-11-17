package ca.prosperpod.interpreter;

import ca.prosperpod.core.ProsperCore;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Gatekeeper {

    private Session socketSession;
    private Map<String, String> userDefinitions = new HashMap<String, String>();

    public Gatekeeper(Session session) {
        this.socketSession = session;
    }

    private String briefUser() {
        String welcome = "Core connection successful.";
        Integer podCount = ProsperCore.getInstance().getPods().length;
        String podAvailability = "There are currently " + podCount + " pods active. " +
                "Enter a pod using the 'connect' command; e.g. 'connect pod-1'.";
        String commandsInfo = "To see a list of available commands, type 'commands?'.";

        JSONObject briefingJSON = new JSONObject();
        briefingJSON.put("messages", new String[]{welcome, podAvailability, commandsInfo})
                .put("console-command", "set-sys-prefix gatekeeper:");
        return briefingJSON.toString();
    }

    public String onUserMessage(String msg) {
        msg = msg.trim();
        if (msg.equals("connection-did-open")) {
            return briefUser();
        } else if (msg.equals("commands?")) {
            String[] commands = {"define", "forget", "forget-all", "defs", "cls", "lookup"};
            return prefixify("commands:", String.join(", ", commands));
        } else if (msg.startsWith("define") || msg.equals("def") || msg.equals("?define")) {
            if (msg.equals("?define")) {
                return prefixify("usage:", "'define {identifier} {expression}' adds a definition that " +
                        "can be used in later commands. Use the 'defs' command to view your definitions. For example, " +
                        "'define rsi-span 15' adds the definition 'rsi-span' - future references to 'rsi-span' " +
                        "will hence yield the expression '15'; additionally, 'define kangaroo \"world's thiqqest" +
                        " animal\" allows for multi-word definitions.");
            } else {
                return addDefinition(msg);
            }
        } else if (msg.equals("forget-all") || msg.equals("?forget-all")) {
            if (msg.equals("?forget-all")) {
                return prefixify("usage:", "'forget-all' removes all previously saved definitions.");
            } else {
                return removeAllDefinitions();
            }
        } else if (msg.startsWith("forget") || msg.equals("?forget")) {
            if (msg.equals("?forget")) {
                return prefixify("usage:", "'forget {identifier}' removes the definition that corresponds " +
                        "with {definition}.  For example, 'forget rsi-span' removes the entry 'rsi-span' if it was " +
                        "previously defined.");
            } else {
                return removeDefinition(msg);
            }
        } else if (msg.equals("defs") || msg.equals("?defs")) {
            if (msg.equals("?defs")) {
                return prefixify("usage:", "'defs' lists all previously saved definitions.");
            } else {
                return this.showDefinitions();
            }
        } else if (msg.equals("cls") || msg.equals("?cls")) {
            if (msg.equals("?cls")) {
                return prefixify("usage:", "'cls' clears the console screen. Use it when " +
                        "things are getting too cluttered.");
            } else {
                return new JSONObject().put("console-command", "clear-console").toString();
            }
        } else if (msg.startsWith("lookup") || msg.equals("?lookup")) {
            if (msg.equals("?lookup")) {
                return prefixify("usage", "'lookup {identifier}' looks up the the definition " +
                        "of {identifier}.");
            } else {
                return lookupDefinition(msg);
            }
        } else {
            return prefixify("error:", "Command '" + msg.split(" ")[0] + "' is not defined.");
        }
    }

    private String prefixify(String prefix, String message) {
        return new JSONObject().put("prefix", prefix)
                .put("message", message).toString();
    }

    private String addDefinition(String definition) {
        String definitionExpression;
        String definitionIdentifier;
        if (definition.contains("\"")) {
            String expression = StringUtils.substringBetween(definition, "\"");
            if (expression.isEmpty()) {
                return prefixify("error:", "Invalid expression formatting (found an open " +
                                "quote with no matching closing quote).");
            }

            String preString = definition.substring(0, definition.indexOf("\"")).trim();
            String postString = definition.substring(definition.lastIndexOf("\"") + 1).trim();
            String[] preStringSplit = preString.split(" ");
            String[] postStringSplit = postString.split(" ");

            if (preStringSplit.length != 2 || !postString.isEmpty()) {
                return prefixify("error:", "'define {identifier} {expression}' expects " +
                        "2 arguments; received " + (preStringSplit.length - 1 + postStringSplit.length)
                        + ".");
            }

            definitionExpression = expression;
            definitionIdentifier = preStringSplit[1];
        } else {
            String[] definitionComponents = definition.split(" ");
            if (definitionComponents.length != 3) {
                return prefixify("error:", "'define {identifier} {expression}' expects 2 " +
                        "arguments; received " + (definitionComponents.length - 1)+ ".");
            }
            definitionIdentifier = definitionComponents[1];
            definitionExpression = definitionComponents[2];
        }

        userDefinitions.put(definitionIdentifier, definitionExpression);
        return "Definition '" + definitionIdentifier + "' added.";
    }

    private String removeDefinition(String command) {
        String[] components = command.split(" ");
        if (components.length == 2) {
            String key = components[1];
            if (this.userDefinitions.containsKey(key)) {
                this.userDefinitions.remove(key);
                return "Forgot definition '" + key + "'.";
            } else {
                return prefixify("error:", "Definition '" + key + "' does not exist.");
            }
        } else {
            return prefixify("error:", "'forget {identifier}' expects 1 argument; " +
                    "received " + (components.length - 1) + ".");
        }
    }

    private String removeAllDefinitions() {
        this.userDefinitions.clear();
        return "Cleared all definitions.";
    }

    private String showDefinitions() {
        ArrayList<String> definitionStrings = new ArrayList<>();
        for (String key : userDefinitions.keySet()) {
            definitionStrings.add(key + " = " + userDefinitions.get(key));
        }
        String definitions = String.join("; ", definitionStrings);
        if (definitions.isEmpty()) {
            definitions = "(empty)";
        }
        return prefixify("definitions:", definitions + ".");
    }

    private String lookupDefinition(String command) {
        String[] commandComponents = command.split(" ");
        if (commandComponents.length != 2) {
            return prefixify("error:", "'lookup' {identifier} expects 1 argument; " +
                    "received " + (commandComponents.length - 1) + ".");
        }

        String result = userDefinitions.get(commandComponents[1]);
        if (result == null) {
            return "Looked up '" + commandComponents[1] + "', but did not find any such definition.";
        }

        return prefixify("definition:", commandComponents[1] + " = " + result);
    }

    public void transmitString(String string) {
        this.socketSession.getAsyncRemote().sendText(string);
    }
    public void transmitJSON(JSONObject jsonObject) {
        this.transmitString(jsonObject.toString());
    }

}