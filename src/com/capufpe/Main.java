package com.capufpe;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle("Text", locale);
        ArgumentParser argumentParser = ArgumentParsers.newFor("fbquiz").build()
                .description(bundle.getString("globalDescription"));
        argumentParser.addArgument("worksheet")
                .help(bundle.getString("worksheetArgDescription"));
        Map arguments = null;
        try {
            arguments = Collections.unmodifiableMap(argumentParser.parseArgs(args).getAttrs());
        } catch (ArgumentParserException e) {
            argumentParser.handleError(e);
            System.exit(0);
        }
        File worksheet = new File((String) arguments.get("worksheet"));
        Tika tika = new Tika();
        tika.setMaxStringLength((int) worksheet.length());
        String raw = null;
        try {
            raw = tika.parseToString(worksheet);
        } catch (IOException | TikaException e) {
            e.printStackTrace();
            System.exit(1);
        }
        ArrayList<String> contents = (ArrayList<String>) Arrays.stream(raw.split("\n"))
                .collect(Collectors.toList());
        String correctAnswers = "";
        ArrayList<Integer> weeks = new ArrayList<>();
        boolean isFirstWeek = true;
        int firstQuestionPageIndex = 0;
        ArrayList<String> linesToBeRemoved = new ArrayList<>();
        for (String line : contents) {
            if (line.matches("^SEMANA.*")) {
                weeks.add(Character.getNumericValue(line.charAt(7)));
                if (isFirstWeek) {
                    firstQuestionPageIndex = Character.getNumericValue(line.trim().charAt(line.trim().length() - 1));
                    isFirstWeek = false;
                }
            } else if (line.matches("^(\\d+ .* ){5}\\d+ .*\\s*$")) correctAnswers = correctAnswers.concat(line);
            else if (line.matches("^\\s*$") ||
                    (line.matches("^\\d\\s*") &&
                            (Integer.parseInt(line.trim()) == 1 || Integer.parseInt(line.trim()) < firstQuestionPageIndex)))
                linesToBeRemoved.add(line);
        }
        for (String line : linesToBeRemoved) contents.remove(line);

        Scanner scanner = new Scanner(System.in);

        System.out.printf("%s %d, %d %s %d",
                bundle.getString("selectWeekDialog"),
                weeks.get(0),
                // There's always a distance of 31 lines between each week header in the table of contents
                weeks.get(1),
                bundle.getString("or"),
                weeks.get(2));
        int weekIndex = weeks.indexOf(scanner.nextInt());
        System.out.printf("%s [B]iologia [F]ísica [Q]uímica [M]atemática [L]inguagens [H]umanas",
                bundle.getString("selectSubjectDialog"));
        int subjectIndex = "BFQMLH".indexOf(Character.toUpperCase(scanner.next().charAt(0)));
        System.out.printf("%s [P]ré-aula [R]evisão [C]asa", bundle.getString("selectCategoryDialog"));
        int categoryIndex = "PRC".indexOf(Character.toUpperCase(scanner.next().charAt(0)));
        System.out.print(contents.get(weekIndex * 31 + (subjectIndex * 5 + 1) + categoryIndex + 2));
    }
}
