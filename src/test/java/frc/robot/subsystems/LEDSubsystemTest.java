// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.util.Color;
import frc.robot.subsystems.led.LEDStrip;
import frc.robot.subsystems.led.LEDStrip.Pattern;
import frc.robot.subsystems.led.LEDStrip.Section;
import frc.robot.subsystems.led.LEDSubsystem;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LEDSubsystemTest {
  private final boolean MOCK_HARDWARE = false;
  private final int LENGTH = 30;
  private final int MIDDLE_START = 10;
  private final int MIDDLE_END = 20;
  private LEDSubsystem m_ledSubsystem;
  private LEDStrip.Hardware m_ledHardware;

  private LEDStrip m_ledStrip;

  private AddressableLED m_leds;

  @BeforeEach
  public void setup() {
    // Create mock hardware devices
    m_leds = mock(AddressableLED.class);

    // Create hardware object using mock hardware
    m_ledHardware = new LEDStrip.Hardware(MOCK_HARDWARE, m_leds);

    // Create LED strip object
    m_ledStrip = new LEDStrip(m_ledHardware, LENGTH);

    // Create LEDSubsystem object
    m_ledSubsystem = LEDSubsystem.getInstance();

    // Add LED strips to LED subsystem
    m_ledSubsystem.add(m_ledStrip);
  }

  @AfterEach
  public void close() {
    m_ledSubsystem.close();
    m_ledSubsystem = null;
  }

  @Test
  @Order(1)
  @DisplayName("Test if robot can set LED strip to single static solid color")
  public void solidFull() {
    // Initialize buffer captor
    ArgumentCaptor<AddressableLEDBuffer> bufferCaptor = ArgumentCaptor.forClass(AddressableLEDBuffer.class);

    // Set LED pattern
    m_ledStrip.set(Pattern.TEAM_COLOR_SOLID, Section.FULL);

    // Run LED subsystem loop
    m_ledSubsystem.periodic();

    // Verify LEDs are being set
    verify(m_leds, times(1)).setData(bufferCaptor.capture());

    // Verify LED pattern
    for (int i = 0; i < LENGTH; i++) 
      assertEquals(LEDStrip.TEAM_COLOR, bufferCaptor.getValue().getLED(i));
  }

  @Test
  @Order(2)
  @DisplayName("Test if robot can set LED strip start section independently")
  public void startSection() {
    // Initialize buffer captor
    ArgumentCaptor<AddressableLEDBuffer> bufferCaptor = ArgumentCaptor.forClass(AddressableLEDBuffer.class);

    // Set LED pattern
    m_ledStrip.set(Pattern.RED_SOLID, Section.START);
    m_ledStrip.set(Pattern.TEAM_COLOR_SOLID, Section.MIDDLE, Section.END);

    // Run LED subsystem loop
    m_ledSubsystem.periodic();
    
    // Verify LEDs are being set
    verify(m_leds, times(1)).setData(bufferCaptor.capture());

    // Verify LED pattern
    for (int i = 0; i < MIDDLE_START; i++)
      assertEquals(Color.kRed, bufferCaptor.getValue().getLED(i));
    for (int i = MIDDLE_START; i < LENGTH; i++)
      assertEquals(LEDStrip.TEAM_COLOR, bufferCaptor.getValue().getLED(i));
  }

  @Test
  @Order(3)
  @DisplayName("Test if robot can set LED strip middle section independently")
  public void middleSection() {
    // Initialize buffer captor
    ArgumentCaptor<AddressableLEDBuffer> bufferCaptor = ArgumentCaptor.forClass(AddressableLEDBuffer.class);

    // Set LED pattern
    m_ledStrip.set(Pattern.RED_SOLID, Section.MIDDLE);
    m_ledStrip.set(Pattern.TEAM_COLOR_SOLID, Section.START, Section.END);

    // Run LED subsystem loop
    m_ledSubsystem.periodic();
    
    // Verify LEDs are being set
    verify(m_leds, times(1)).setData(bufferCaptor.capture());

    // Verify LED pattern
    for (int i = 0; i < LENGTH / 3; i++)
      assertEquals(LEDStrip.TEAM_COLOR, bufferCaptor.getValue().getLED(i));
    for (int i = MIDDLE_START; i < MIDDLE_END; i++)
      assertEquals(Color.kRed, bufferCaptor.getValue().getLED(i));
    for (int i = MIDDLE_END; i < LENGTH; i++)
      assertEquals(LEDStrip.TEAM_COLOR, bufferCaptor.getValue().getLED(i));
  }

  @Test
  @Order(4)
  @DisplayName("Test if robot can set LED strip end section independently")
  public void endSection() {
    // Initialize buffer captor
    ArgumentCaptor<AddressableLEDBuffer> bufferCaptor = ArgumentCaptor.forClass(AddressableLEDBuffer.class);

    // Set LED pattern
    m_ledStrip.set(Pattern.RED_SOLID, Section.END);
    m_ledStrip.set(Pattern.TEAM_COLOR_SOLID, Section.START, Section.MIDDLE);

    // Run LED subsystem loop
    m_ledSubsystem.periodic();
    
    // Verify LEDs are being set
    verify(m_leds, times(1)).setData(bufferCaptor.capture());

    // Verify LED pattern
    for (int i = 0; i < MIDDLE_END; i++)
      assertEquals(LEDStrip.TEAM_COLOR, bufferCaptor.getValue().getLED(i));
    for (int i = MIDDLE_END; i < LENGTH; i++)
      assertEquals(Color.kRed, bufferCaptor.getValue().getLED(i));
  }
}
