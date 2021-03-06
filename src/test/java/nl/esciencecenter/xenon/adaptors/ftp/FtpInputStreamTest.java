/**
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon.adaptors.ftp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.Path;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Test;

public class FtpInputStreamTest {
    @Test
    public void close_callTwice_completePendingCommandOnlyOnce() throws IOException {
        // Arrange
        InputStream stream = new ByteArrayInputStream(new byte[4]);
        FTPClient ftpClient = mock(FTPClient.class);
        FtpInputStream ftpInputStream = new FtpInputStream(stream, ftpClient, mock(Path.class), mock(FtpFiles.class));

        // Act
        ftpInputStream.close();
        ftpInputStream.close();

        // Assert
        verify(ftpClient).completePendingCommand();
    }

    @Test
    public void close_callOnce_fileSystemIsClosed() throws IOException, XenonException {
        // Arrange
        InputStream stream = new ByteArrayInputStream(new byte[4]);
        FtpFiles ftpFiles = mock(FtpFiles.class);
        FtpInputStream ftpInputStream = new FtpInputStream(stream, mock(FTPClient.class), mock(Path.class), ftpFiles);

        // Act
        ftpInputStream.close();

        // Assert
        verify(ftpFiles).close(null);
    }
}
