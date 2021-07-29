package com.itransition.courses.project3;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

public class GameEngine {
    private String[] inputArgs;
    private String stringKey;

    public void startGame(String[] args) {
        inputArgs = args;
        if (!isInputArgsValid()) return;
        String deviceMove = getDeviceMove();
        String hmacDeviceMove = encryptDeviceMove(deviceMove);
        System.out.printf("HMAC: %s\n", hmacDeviceMove);
        String userMove = takeUserMove();
        GameResult result = determineWinner(userMove, deviceMove);
        printResults(userMove, deviceMove, stringKey, result);
    }

    public void printResults(String userMove, String deviceMove, String stringKey, GameResult result) {
        System.out.printf("""
                Your move: %s
                Computer move: %s
                %s
                HMAC key: %s%n""", userMove, deviceMove, result.toString(), stringKey);
    }

    private boolean isInputArgsValid() {
        if (inputArgs.length < 3) {
            System.out.println("You must provide at least 3 arguments\n" +
                    "Example: GeneralisedRoshamboGame.jar Rock Paper Scissors");
            return false;
        } else if (inputArgs.length % 2 == 0) {
            System.out.println("The number of arguments must be odd\n" +
                    "Example: GeneralisedRoshamboGame.jar Rock Paper Scissors");
            return false;
        } else if (!(inputArgs.length == Arrays.stream(inputArgs).collect(Collectors.toSet()).size())) {
            System.out.println("Arguments must be distinct!");
            return false;
        }
        return true;
    }

    private String getDeviceMove() {
        SecureRandom random = new SecureRandom();
        return inputArgs[random.nextInt(inputArgs.length)];
    }

    private String encryptDeviceMove(String move) {
        final String ALGORITHM = "HmacSHA256";
        String resultHmac = "";
        try {
            SecureRandom random = new SecureRandom();
            Mac mac = Mac.getInstance(ALGORITHM);
            byte[] keyBytes = new byte[128];
            random.nextBytes(keyBytes);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            mac.init(secretKeySpec);
            byte[] byteMove = move.getBytes(StandardCharsets.UTF_8);
            resultHmac = toHex(mac.doFinal(byteMove));
            stringKey = toHex(mac.doFinal(keyBytes));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return resultHmac;
    }

    private String toHex(byte[] data) {
        final StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private String takeUserMove() {
        String userMove = "";
        try (Scanner scanner = new Scanner(System.in)) {
            printPossibleMoves();
            userMove = scanner.next();
            while (!isUserMoveValid(userMove)) {
                printPossibleMoves();
                userMove = scanner.next();
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return inputArgs[Integer.parseInt(userMove) - 1];
    }

    private void printPossibleMoves() {
        System.out.println("Available moves:");
        int counter = 1;
        for (String arg : inputArgs) {
            System.out.printf("%d - %s\n", counter, arg);
            counter++;
        }
        System.out.print("Enter your move: ");
    }

    private boolean isUserMoveValid(String move) {
        if (StringUtils.isNumeric(move)) {
            return Integer.parseInt(move) > 0 && Integer.parseInt(move) <= inputArgs.length;
        } else {
            return false;
        }
    }

    private GameResult determineWinner(String userMove, String deviceMove) {
        String[] centerUserMove = centralizeMoveInArray(userMove);
        int range = inputArgs.length / 2;
        int centralisedUserMoveIndex = Arrays.asList(centerUserMove).indexOf(userMove);
        String[] losingToUser = Arrays.copyOfRange(centerUserMove, centralisedUserMoveIndex - range, centralisedUserMoveIndex);
        String[] defeatingUser = Arrays.copyOfRange(centerUserMove, centralisedUserMoveIndex + 1, centralisedUserMoveIndex + range + 1);

        if (Arrays.asList(defeatingUser).contains(deviceMove)) {
            return GameResult.LOSE;
        } else if (Arrays.asList(losingToUser).contains(deviceMove)) {
            return GameResult.WIN;
        } else return GameResult.DRAW;
    }

    private String[] centralizeMoveInArray(String move) {
        String[] centerDeviceMove = new String[(inputArgs.length * 2) - 1];
        int deviceMoveIndex = Arrays.asList(inputArgs).indexOf(move);
        if (deviceMoveIndex != inputArgs.length / 2) {
            if (deviceMoveIndex <= inputArgs.length / 2) {
                for (int i = 0, j = deviceMoveIndex + 1; i < centerDeviceMove.length - deviceMoveIndex; i++, j++) {
                    if (j == inputArgs.length) {
                        j = 0;
                    }
                    centerDeviceMove[i] = inputArgs[j];
                }
            } else {
                for (int i = 0, j = 0; i < centerDeviceMove.length - (inputArgs.length - deviceMoveIndex - 1); i++, j++) {
                    if (j == inputArgs.length) {
                        j = 0;
                    }
                    centerDeviceMove[i] = inputArgs[j];
                }
            }
        }
        return centerDeviceMove[0] == null ? inputArgs : centerDeviceMove;
    }
}
