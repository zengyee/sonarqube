/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.api.batch.fs.internal;

import java.io.File;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class DefaultInputFileTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void test() throws Exception {
    Path baseDir = temp.newFolder().toPath();
    DefaultInputFile inputFile = new DefaultInputFile("ABCDE", "src/Foo.php")
      .setModuleBaseDir(baseDir)
      .setLines(42)
      .setLanguage("php")
      .setStatus(InputFile.Status.ADDED)
      .setType(InputFile.Type.TEST);

    assertThat(inputFile.relativePath()).isEqualTo("src/Foo.php");
    assertThat(new File(inputFile.relativePath())).isRelative();
    assertThat(inputFile.absolutePath()).endsWith("Foo.php");
    assertThat(new File(inputFile.absolutePath())).isAbsolute();
    assertThat(inputFile.language()).isEqualTo("php");
    assertThat(inputFile.status()).isEqualTo(InputFile.Status.ADDED);
    assertThat(inputFile.type()).isEqualTo(InputFile.Type.TEST);
    assertThat(inputFile.lines()).isEqualTo(42);
  }

  @Test
  public void test_equals_and_hashcode() throws Exception {
    DefaultInputFile f1 = new DefaultInputFile("ABCDE", "src/Foo.php");
    DefaultInputFile f1a = new DefaultInputFile("ABCDE", "src/Foo.php");
    DefaultInputFile f2 = new DefaultInputFile("ABCDE", "src/Bar.php");

    assertThat(f1).isEqualTo(f1);
    assertThat(f1).isEqualTo(f1a);
    assertThat(f1).isNotEqualTo(f2);
    assertThat(f1.equals("foo")).isFalse();
    assertThat(f1.equals(null)).isFalse();

    assertThat(f1.hashCode()).isEqualTo(f1.hashCode());
    assertThat(f1.hashCode()).isEqualTo(f1a.hashCode());
  }

  @Test
  public void test_toString() throws Exception {
    DefaultInputFile file = new DefaultInputFile("ABCDE", "src/Foo.php");
    assertThat(file.toString()).isEqualTo("[moduleKey=ABCDE, relative=src/Foo.php, basedir=null]");
  }

  @Test
  public void checkValidPointer() {
    DefaultInputFile file = new DefaultInputFile("ABCDE", "src/Foo.php");
    file.setLines(2);
    file.setOriginalLineOffsets(new int[] {0, 10});
    file.setLastValidOffset(15);
    assertThat(file.newPointer(1, 0).line()).isEqualTo(1);
    assertThat(file.newPointer(1, 0).lineOffset()).isEqualTo(0);
    // Don't fail
    file.newPointer(1, 9);
    file.newPointer(2, 0);
    file.newPointer(2, 5);

    try {
      file.newPointer(0, 1);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("0 is not a valid line for a file");
    }
    try {
      file.newPointer(3, 1);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("3 is not a valid line for pointer. File [moduleKey=ABCDE, relative=src/Foo.php, basedir=null] has 2 line(s)");
    }
    try {
      file.newPointer(1, -1);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("-1 is not a valid line offset for a file");
    }
    try {
      file.newPointer(1, 10);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("10 is not a valid line offset for pointer. File [moduleKey=ABCDE, relative=src/Foo.php, basedir=null] has 9 character(s) at line 1");
    }
  }

  @Test
  public void checkValidPointerUsingGlobalOffset() {
    DefaultInputFile file = new DefaultInputFile("ABCDE", "src/Foo.php");
    file.setLines(2);
    file.setOriginalLineOffsets(new int[] {0, 10});
    file.setLastValidOffset(15);
    assertThat(file.newPointer(0).line()).isEqualTo(1);
    assertThat(file.newPointer(0).lineOffset()).isEqualTo(0);

    assertThat(file.newPointer(9).line()).isEqualTo(1);
    assertThat(file.newPointer(9).lineOffset()).isEqualTo(9);

    assertThat(file.newPointer(10).line()).isEqualTo(2);
    assertThat(file.newPointer(10).lineOffset()).isEqualTo(0);

    assertThat(file.newPointer(15).line()).isEqualTo(2);
    assertThat(file.newPointer(15).lineOffset()).isEqualTo(5);

    try {
      file.newPointer(-1);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("-1 is not a valid offset for a file");
    }

    try {
      file.newPointer(16);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("16 is not a valid offset for file [moduleKey=ABCDE, relative=src/Foo.php, basedir=null]. Max offset is 15");
    }
  }

  @Test
  public void checkValidRange() {
    DefaultInputFile file = new DefaultInputFile("ABCDE", "src/Foo.php");
    file.initMetadata("bla bla a\nabcde");
    assertThat(file.newRange(file.newPointer(1, 0), file.newPointer(2, 1)).start().line()).isEqualTo(1);
    // Don't fail
    file.newRange(file.newPointer(1, 0), file.newPointer(1, 1));
    file.newRange(file.newPointer(1, 0), file.newPointer(1, 9));
    file.newRange(file.newPointer(1, 0), file.newPointer(2, 0));
    assertThat(file.newRange(file.newPointer(1, 0), file.newPointer(2, 5))).isEqualTo(file.newRange(0, 15));

    try {
      file.newRange(file.newPointer(1, 0), file.newPointer(1, 0));
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("Start pointer [line=1, lineOffset=0] should be before end pointer [line=1, lineOffset=0]");
    }
    try {
      file.newRange(file.newPointer(1, 0), file.newPointer(1, 10));
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("10 is not a valid line offset for pointer. File [moduleKey=ABCDE, relative=src/Foo.php, basedir=null] has 9 character(s) at line 1");
    }
  }

  @Test
  public void selectLine() {
    DefaultInputFile file = new DefaultInputFile("ABCDE", "src/Foo.php");
    file.initMetadata("bla bla a\nabcde\n\nabc");
    assertThat(file.selectLine(1).start().line()).isEqualTo(1);
    assertThat(file.selectLine(1).start().lineOffset()).isEqualTo(0);
    assertThat(file.selectLine(1).end().line()).isEqualTo(1);
    assertThat(file.selectLine(1).end().lineOffset()).isEqualTo(9);

    // Don't fail when selecting empty line
    assertThat(file.selectLine(3).start().line()).isEqualTo(3);
    assertThat(file.selectLine(3).start().lineOffset()).isEqualTo(0);
    assertThat(file.selectLine(3).end().line()).isEqualTo(3);
    assertThat(file.selectLine(3).end().lineOffset()).isEqualTo(0);

    try {
      file.selectLine(5);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("5 is not a valid line for pointer. File [moduleKey=ABCDE, relative=src/Foo.php, basedir=null] has 4 line(s)");
    }
  }

  @Test
  public void checkValidRangeUsingGlobalOffset() {
    DefaultInputFile file = new DefaultInputFile("ABCDE", "src/Foo.php");
    file.setLines(2);
    file.setOriginalLineOffsets(new int[] {0, 10});
    file.setLastValidOffset(15);
    TextRange newRange = file.newRange(10, 13);
    assertThat(newRange.start().line()).isEqualTo(2);
    assertThat(newRange.start().lineOffset()).isEqualTo(0);
    assertThat(newRange.end().line()).isEqualTo(2);
    assertThat(newRange.end().lineOffset()).isEqualTo(3);
  }

  @Test
  public void testRangeOverlap() {
    DefaultInputFile file = new DefaultInputFile("ABCDE", "src/Foo.php");
    file.setLines(2);
    file.setOriginalLineOffsets(new int[] {0, 10});
    file.setLastValidOffset(15);
    // Don't fail
    assertThat(file.newRange(file.newPointer(1, 0), file.newPointer(1, 1)).overlap(file.newRange(file.newPointer(1, 0), file.newPointer(1, 1)))).isTrue();
    assertThat(file.newRange(file.newPointer(1, 0), file.newPointer(1, 1)).overlap(file.newRange(file.newPointer(1, 0), file.newPointer(1, 2)))).isTrue();
    assertThat(file.newRange(file.newPointer(1, 0), file.newPointer(1, 1)).overlap(file.newRange(file.newPointer(1, 1), file.newPointer(1, 2)))).isFalse();
    assertThat(file.newRange(file.newPointer(1, 2), file.newPointer(1, 3)).overlap(file.newRange(file.newPointer(1, 0), file.newPointer(1, 2)))).isFalse();
  }
}
