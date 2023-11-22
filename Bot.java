package dnd.diceroller;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static java.lang.Math.toIntExact;

public class Bot extends TelegramLongPollingBot {
    HashMap<Long, Stack<Integer>> diceSet = new HashMap<Long, Stack<Integer>>();
    HashMap<Long, String> diceNames = new HashMap<Long, String>();
    InlineKeyboardButton d100 = InlineKeyboardButton.builder()
            .text("d100").callbackData("100")
            .build();
    InlineKeyboardButton d20 = InlineKeyboardButton.builder()
            .text("d20").callbackData("20")
            .build();
    InlineKeyboardButton d12 = InlineKeyboardButton.builder()
            .text("d12").callbackData("12")
            .build();
    InlineKeyboardButton d10 = InlineKeyboardButton.builder()
            .text("d10").callbackData("10")
            .build();
    InlineKeyboardButton d8 = InlineKeyboardButton.builder()
            .text("d8").callbackData("8")
            .build();
    InlineKeyboardButton d6 = InlineKeyboardButton.builder()
            .text("d6").callbackData("6")
            .build();
    InlineKeyboardButton d4 = InlineKeyboardButton.builder()
            .text("d4").callbackData("4")
            .build();
    InlineKeyboardButton go = InlineKeyboardButton.builder()
            .text("Roll!").callbackData("roll")
            .build();
    InlineKeyboardButton clear = InlineKeyboardButton.builder()
            .text("CLEAR!").callbackData("clear")
            .build();
    private final InlineKeyboardMarkup kb = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(d20,d12,d10))
            .keyboardRow(List.of(d8,d6,d4))
            .keyboardRow(List.of(d100,go, clear))
            .build();
    public void sendMenu(Long who, String txt, InlineKeyboardMarkup kb){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(txt)
                .replyMarkup(kb)
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public String getBotUsername() {
        return Config.username;
    }

    @Override
    public String getBotToken() {
        return Config.token;
    }


    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage()){
            if(update.getMessage().isCommand()){
                if(update.getMessage().getText().equals("/roll")){
                    var id = update.getMessage().getFrom().getId();
                    sendMenu(id,"DICE: ",kb);
                }
            }
        }
        else if(update.hasCallbackQuery()){
            Long usid = update.getCallbackQuery().getFrom().getId();
            String qId = update.getCallbackQuery().getId();
            String data = update.getCallbackQuery().getData();
            int message_id = update.getCallbackQuery().getMessage().getMessageId();
            if(!diceSet.containsKey(usid)) diceSet.put(usid, new Stack<Integer>());
            if(!diceNames.containsKey(usid)) diceNames.put(usid, "DICE: ");
            try {
                tapButton(usid,qId,data,message_id, diceSet, diceNames);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void tapButton(Long id, String queryId, String data, int msgId,
                          HashMap<Long, Stack<Integer>> thisSet,
                          HashMap<Long, String> nameSet) throws TelegramApiException {
        if(nameSet.get(id).contains("RESULT")) nameSet.replace(id,"DICE: ");
        switch (data) {
            case "20" -> {
                thisSet.get(id).add(20);
                nameSet.replace(id,nameSet.get(id).concat(" 20;"));
            }
            case "12" -> {
                thisSet.get(id).add(12);
                nameSet.replace(id,nameSet.get(id).concat(" 12;"));
            }
            case "10" -> {
                thisSet.get(id).add(10);
                nameSet.replace(id,nameSet.get(id).concat(" 10;"));
            }
            case "8" -> {
                thisSet.get(id).add(8);
                nameSet.replace(id,nameSet.get(id).concat(" 8;"));
            }
            case "6" -> {
                thisSet.get(id).add(6);
                nameSet.replace(id,nameSet.get(id).concat(" 6;"));
            }
            case "4" -> {
                thisSet.get(id).add(4);
                nameSet.replace(id,nameSet.get(id).concat(" 4;"));
            }
            case "roll" -> {
                int output;
                int sum=0;
                int max=0;
                int min=999;
                nameSet.replace(id,"RESULT: ");
                while(!thisSet.get(id).isEmpty()){
                    output = (int)(1+Math.random()*(thisSet.get(id).pop()));
                    if(output>max) max=output;
                    if(output<min) min=output;
                    sum=sum+output;
                    nameSet.replace(id,nameSet.get(id).concat(output+"; "));
                }
                nameSet.replace(id,nameSet.get(id).concat("\nSum is: "+sum+"\nMin is: "+min+"\nMax is: "+max));
                thisSet.get(id).clear();
            }
            case "100" -> {
                thisSet.get(id).add(100);
                nameSet.replace(id,nameSet.get(id).concat(" 100;"));
            }
            case "clear" -> {
                thisSet.get(id).clear();
                nameSet.replace(id,"DICE: ");
            }
        }

        AnswerCallbackQuery close = AnswerCallbackQuery.builder()
                .callbackQueryId(queryId)
                .build();
        EditMessageText newTxt = new EditMessageText();
        newTxt.setChatId(id.toString());
        newTxt.setMessageId(toIntExact(msgId));
        newTxt.setText(nameSet.get(id));

        EditMessageReplyMarkup newKb = new EditMessageReplyMarkup();
        newKb.setChatId(id.toString());
        newKb.setMessageId(toIntExact(msgId));
        newKb.setReplyMarkup(kb);

        execute(close);
        execute(newTxt);
        execute(newKb);

    }
}