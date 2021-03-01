/**
 * <pre>
 * ##########################################################################################################
 * ######                            This file is part of Java FSUIPC SDK                              ######
 * ######                                        Version: 1.0                                          ######
 * ######         Based upon 64 bit Java SDK by Paul Henty who amended 32 bit SDK by Mark Burton       ######
 * ######                                   ©2020, Radek Henys                                         ######
 * ######                         All rights .... well, this will be LGPL or so                        ######
 * ######                                   http:\\mouseviator.com                                     ######
 * ##########################################################################################################
 * </pre>
 */
package com.mouseviator.fsuipc.testing;

import java.text.MessageFormat;
import javax.swing.JOptionPane;
import static org.junit.Assert.fail;

/**
 * This is a class providing basic functionality for interactive test. It contains methods to display to the tester what will be tested and
 * to ask him/her whether the test met the expectations.
 * 
 * @author Murdock
 */
public class InteractiveTest {
    protected static boolean interactive = true;
    
    /**
     * Will show information message box to inform tester about test being performed.
     * 
     * @param testInfo Message to display.
     */
    protected void showTestInfo(String testInfo) {
        if (!interactive) {
            return;
        }
        
        JOptionPane.showMessageDialog(null, MessageFormat.format("<html>{0}</html>", testInfo), "Test info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Will show question dialog asking tester about the test result. 
     * 
     * @param questionText Question text.
     * @param optionType What buttons to show.
     * @param correctOption Which button is for correct (not fail) test result.
     */
    protected void askTestResult(String questionText, int optionType, int correctOption) {
        if (!interactive) {
            return;
        }
        
        int answer = JOptionPane.showConfirmDialog(null, MessageFormat.format("<html>{0}</html>", questionText), "Please verify test result", optionType, JOptionPane.QUESTION_MESSAGE);
        if (answer == correctOption) {
            System.out.println("Test PASSED!");
        } else {
            fail("Test FAILED!");
        }
    }
}
