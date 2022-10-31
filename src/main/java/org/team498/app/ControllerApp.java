package org.team498.app;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class ControllerApp extends CommandBase{
    //normally, I would make this a separate thing, but we want to run it at the same time as the robot
    //also this is the easiest way to access the controllers lol
    private FileOutputStream outFS;
    private PrintWriter printer;
    private XboxController controller;
    private String fileName;
    private Timer timer = new Timer();
    public ControllerApp(XboxController controller, String fileName) {
        this.controller = controller;
        this.fileName = fileName;
        openFile();
    }
    @Override
    public void initialize() {
        //printer.print("{\n");
        timer.start();
    }
    @Override
    public void execute() {
        double timeStamp = timer.get();
        double dx = controller.getLeftY();
        double dy = controller.getLeftX();
        double dr = controller.getRightX();
        printInfo(timeStamp, dx, dy, dr);
    }
    @Override
    public void end(boolean interrupted) {
        //printer.print("\n}");
        closeFile();
        timer.stop();
        timer.reset();
    }

    private void printInfo(double timeStamp, double dx, double dy, double dr) {
        printer.printf("{ %.3f, %.4f, %.4f, %.4f },\n", timeStamp, dx, dy, dr);
    }

    private void openFile() {
        try {
            outFS = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        printer = new PrintWriter(outFS);
    }
    private void closeFile() {
        try {
            outFS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}