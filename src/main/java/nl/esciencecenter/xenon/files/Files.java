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
package nl.esciencecenter.xenon.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.InvalidSchemeException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.credentials.Credential;

/**
 * Files represents the Files interface Xenon.
 * 
 * This interface contains various methods for creating and closing 
 * FileSystems, creating Paths and operations on these Paths.
 * 
 * @version 1.0
 * @since 1.0
 */
public interface Files {

    /**
     * Create a new FileSystem that represents a (possibly remote) data store 
     * at the <code>location</code>, using the <code>scheme</code> and 
     * <code>credentials</code> to get access. Make sure to always close 
     * {@code FileSystem} instances by calling {@code close(FileSystem)} when
     * you no longer need them, otherwise their associated resources remain 
     * allocated.
     * 
     * @param scheme
     *            the scheme to use to access the FileSystem.
     * @param location
     *            the location of the FileSystem, may be null for a local file system.
     * @param credential
     *            the Credentials to use to get access to the FileSystem.
     * @param properties
     *            optional properties to use when creating the FileSystem.
     * 
     * @return the new FileSystem.
     * 
     * @throws UnknownPropertyException
     *             If a unknown property was provided.
     * @throws InvalidPropertyException
     *             If a known property was provided with an invalid value.
     * @throws InvalidSchemeException
     *             If the scheme was invalid.
     * @throws InvalidLocationException
     *             If the location was invalid.
     * @throws InvalidCredentialException
     *             If the credential is invalid to access the location.
     * 
     * @throws XenonException
     *             If the creation of the FileSystem failed.
     */
    FileSystem newFileSystem(String scheme, String location, Credential credential, Map<String, String> properties) 
            throws XenonException;
        
    /**
     * Create a new Path that represents a (possibly non existing) location on <code>filesystem.</code>
     *  
     * @param filesystem
     *            the FileSystem for which to create the path.
     * @param location
     *            the location relative to the root of the given FileSystem.
     * 
     * @return the resulting Path.
     * 
     * @throws XenonException
     *             If an I/O error occurred.
     */
    Path newPath(FileSystem filesystem, RelativePath location) throws XenonException;

    /**
     * Close a FileSystem.
     * 
     * @param filesystem
     *            the FileSystem to close.
     * 
     * @throws XenonException
     *             If the FileSystem failed to close or if an I/O error occurred.
     */
    void close(FileSystem filesystem) throws XenonException;

    /**
     * Return if the connection to the FileSystem is open.
     * 
     * @param filesystem
     *          the FileSystem to test.
     * 
     * @throws XenonException
     *          if the test failed or an I/O error occurred.
     * @return
     *          if the connection to the FileSystem is open.                     
     */
    boolean isOpen(FileSystem filesystem) throws XenonException;

    /**
     * Copy an existing source file or symbolic link to a target file.
     * <p>
     * When using copy, the following rules apply:
     * </p>
     * <ul>
     * <li>
     * Both source and target must NOT be a directory.
     * </li>
     * <li>
     * The parent of the target path (e.g. <code>target.getParent</code>) must exist.
     * </li>
     * <li>
     * If the target is equal to the source this method has no effect.
     * </li>
     * <li>
     * If the source is a link, the path to which it refers will be copied, not the link itself.
     * </li>
     * </ul>
     * <p>
     * In addition, the <code>options</code> parameter determines how the copy is performed:
     * </p>
     * <ul>
     * <li><code>CREATE</code> (default): Create a new target file and copy to it. Fail if the target already exists.</li>
     * 
     * <li><code>REPLACE</code>: Replace target if it already exists. If the target does not exist it will be created.</li>
     * 
     * <li><code>IGNORE</code>: Ignore copy if the target already exists. If the target does not exist it will be created.</li>
     * 
     * <li><code>APPEND</code>: The data in source will appended to target. Fails if the target does not exist.</li>
     * 
     * <li><code>RESUME</code>: A copy from source to target will be resumed. This fails if the target does not exist. To resume 
     * a copy, the size of the target is used as the start position in the source. All data from the source after this start
     * position is append to the target. For example, if the target contains 100 bytes (0-99) and the source 200 bytes (0-199),
     * the data at bytes 100-199 in the source will be append to target. By default, there is no verification that the existing
     * data in target corresponds to the data in source.</li>
     * </ul>
     * <p>
     * Note that the five options above are mutually exclusive. Only one can be selected at a time. If more than one of these 
     * options is provided, an exception will be thrown. 
     * </p>
     * <p>
     * The following options modify the behavior of the copy operation:
     * </p>
     * <ul>
     * <li><code>VERIFY</code> (can only be used in combination with <code>RESUME</code>): When resuming a copy, verify that the
     * existing data in target corresponds to the data in source.</li>
     * 
     * <li><code>ASYNCHRONOUS</code>: Perform an asynchronous copy. Instead of blocking until the copy is complete, the call
     * returns immediately and the copy is performed in the background.</li>
     * </ul>
     * <p> 
     * If the <code>ASYNCHRONOUS</code> option is provided, a {@link Copy} is returned that can be used to retrieve the status of
     * the copy operation (in a {@link CopyStatus}) or cancel it. Any exceptions produced during the copy operation are also
     * stored in the {@link CopyStatus}.
     * </p>
     * <p>
     * If the <code>ASYNCHRONOUS</code> option is not provided, the copy will block until it is completed and <code>null</code>
     * will be returned.
     * </p>
     * 
     * @param source
     *            the existing source file or link.
     * @param target
     *            the target path.
     * @param options
     *            options for the copy operation.
     * @return a {@link CopyStatus} if the copy is asynchronous or <code>null</code> if it is blocking.
     * 
     * @throws NoSuchPathException
     *             If the source file does not exist, the target parent directory does not exist, or the target file does not
     *             exist and the <code>APPEND</code> or <code>RESUME</code> option is provided.
     * @throws PathAlreadyExistsException
     *             If the target file already exists.
     * @throws IllegalSourcePathException
     *             If the source is a directory.
     * @throws IllegalTargetPathException
     *             If the target is a directory.
     * @throws InvalidCopyOptionsException
     *             If a conflicting set of copy options is provided.
     * @throws InvalidResumeTargetException
     *             If the data in the target of a resume does not match the data in the source. 
     * @throws XenonException
     *             If an I/O error occurred.
     */
    Copy copy(Path source, Path target, CopyOption... options) throws XenonException;

    /**
     * Move or rename an existing source path to a non-existing target path.
     * <p>
     * The parent of the target path (e.g. <code>target.getParent</code>) must exist.
     * 
     * If the target is equal to the source this method has no effect.
     * 
     * If the source is a link, the link itself will be moved, not the path to which it refers.
     * 
     * If the source is a directory, it will be renamed to the target. This implies that a moving a directory between physical
     * locations may fail.
     * </p>
     * @param source
     *            the existing source path.
     * @param target
     *            the non existing target path.
     * 
     * @throws NoSuchPathException
     *             If the source file does not exist or the target parent directory does not exist.
     * @throws PathAlreadyExistsException
     *             If the target file already exists.
     * @throws XenonException
     *             If the move failed.
     */
    void move(Path source, Path target) throws XenonException;

    /**
     * Retrieve the status of an asynchronous copy.
     * 
     * @param copy
     *            the asynchronous copy for which to retrieve the status.
     * 
     * @return a {@link CopyStatus} containing the status of the asynchronous copy.
     * 
     * @throws NoSuchCopyException
     *             If the copy is not known.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    CopyStatus getCopyStatus(Copy copy) throws XenonException;

    /**
     * Cancel a copy operation.
     * 
     * @param copy
     *            the asynchronous copy which to cancel.
     * 
     * @throws NoSuchCopyException
     *             If the copy is not known.
     * @throws XenonException
     *             If an I/O error occurred.
     * @return
     *             a {@link CopyStatus} containing the status of the copy.
     *                         
     */
    CopyStatus cancelCopy(Copy copy) throws XenonException;

    /**
     * Creates a new directory, failing if the directory already exists. All nonexistent parent directories are also created.
     * 
     * @param dir
     *            the directory to create.
     * 
     * @throws PathAlreadyExistsException
     *             If the directory already exists or if a parent directory could not be created because a file with the same name
     *             already exists.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    void createDirectories(Path dir) throws XenonException;

    /**
     * Creates a new directory, failing if the directory already exists.
     * 
     * @param dir
     *            the directory to create.
     * 
     * @throws PathAlreadyExistsException
     *             If the directory already exists.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    void createDirectory(Path dir) throws XenonException;

    /**
     * Creates a new empty file, failing if the file already exists.
     * 
     * @param path
     *            the file to create.
     * 
     * @throws PathAlreadyExistsException
     *             If the directory already exists.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    void createFile(Path path) throws XenonException;

    /**
     * Deletes an existing path.
     * 
     * If path is a symbolic link the symbolic link is removed and the symbolic link's target is not deleted.
     * 
     * @param path
     *            the path to delete.
     * 
     * @throws XenonException
     *             If an I/O error occurred.
     */
    void delete(Path path) throws XenonException;

    /**
     * Tests if a path exists.
     * 
     * @param path
     *            the path to test.
     * 
     * @return If the path exists.
     * 
     * @throws XenonException
     *             If an I/O error occurred.
     */
    boolean exists(Path path) throws XenonException;

    /**
     * Create a DirectoryStream that iterates over all entries in the directory <code>dir</code>.
     * 
     * @param dir
     *            the target directory.
     * @return a new DirectoryStream that iterates over all entries in the given directory.
     * 
     * @throws NoSuchPathException
     *             If a directory does not exists.
     * @throws IllegalSourcePathException
     *             If dir is not a directory.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    DirectoryStream<Path> newDirectoryStream(Path dir) throws XenonException;

    /**
     * Create a DirectoryStream that iterates over all entries in the directory <code>dir</code> that are accepted by the filter.
     * 
     * @param dir
     *            the target directory.
     * @param filter
     *            the filter.
     * 
     * @return a new DirectoryStream that iterates over all entries in the directory <code>dir</code>.
     * 
     * @throws NoSuchPathException
     *             If a directory does not exists.
     * @throws IllegalSourcePathException
     *             If dir is not a directory.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter filter) throws XenonException;

    /**
     * Create a DirectoryStream that iterates over all PathAttributePair entries in the directory <code>dir</code>.
     * 
     * @param dir
     *            the target directory.
     * 
     * @return a new DirectoryStream that iterates over all PathAttributePair entries in the directory <code>dir</code>.
     * 
     * @throws NoSuchPathException
     *             If a directory does not exists.
     * @throws IllegalSourcePathException
     *             If dir is not a directory.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir) throws XenonException;

    /**
     * Create a DirectoryStream that iterates over all PathAttributePair entries in the directory <code>dir</code> that are
     * accepted by the filter.
     * 
     * @param dir
     *            the target directory.
     * @param filter
     *            the filter.
     * 
     * @return a new DirectoryStream that iterates over all entries in the directory <code>dir</code>.
     * 
     * @throws NoSuchPathException
     *             If a directory does not exists.
     * @throws IllegalSourcePathException
     *             If dir is not a directory.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    DirectoryStream<PathAttributesPair> newAttributesDirectoryStream(Path dir, DirectoryStream.Filter filter)
            throws XenonException;

    /**
     * Open an existing file and return an {@link InputStream} to read from this file.
     * 
     * @param path
     *            the existing file to read.
     * 
     * @return the {@link InputStream} to read from the file.
     * 
     * @throws NoSuchPathException
     *             If a file does not exists.
     * @throws IllegalSourcePathException
     *             If path not file.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    InputStream newInputStream(Path path) throws XenonException;

    /**
     * Open an file and return an {@link OutputStream} to write to this file.
     * <p>
     * The options determine how the file is opened, if a new file is created, if the existing data in the file is preserved, and
     * if the file should be written or read:
     * </p>
     * <ul>
     * <li>
     * If the <code>CREATE</code> option is specified, a new file will be created and an exception is thrown if the file already
     * exists.
     * </li>
     * <li>
     * If the <code>OPEN_EXISTING</code> option is specified, an existing file will be opened, and an exception is thrown if the
     * file does not exist.
     * </li>
     * <li>
     * If the <code>OPEN_OR_CREATE</code> option is specified, an attempt will be made to open an existing file. If it does not
     * exist a new file will be created.
     * </li>
     * <li>
     * If the <code>APPEND</code> option is specified, data will be added to the end of the file. No existing data will be
     * overwritten.
     * </li>
     * <li>
     * If the <code>TRUNCATE</code> option is specified, any existing data in the file will be deleted (resulting in a file of
     * size 0). The data will then be appended from the beginning of the file.
     * </li>
     * </ul>
     * <p>
     * One of <code>CREATE</code>, <code>OPEN_EXISTING</code> or <code>OPEN_OR_CREATE</code> must be specified. Specifying more
     * than one will result in an exception.
     * </p>
     * <p>
     * Either <code>APPEND</code> or <code>TRUNCATE</code> must be specified. Specifying both will result in an exception.
     * </p>
     * <p>
     * The <code>READ</code> option must not be set. If it is set, an exception will be thrown.
     * </p>
     * <p>
     * If the <code>WRITE</code> option is specified, the file is opened for writing. As this is the default behavior, the
     * <code>WRITE</code> option may be omitted.
     * </p>
     * @param path
     *            the target file for the OutputStream.
     * @param options
     *            the options to use for opening this file.
     * 
     * @return the {@link OutputStream} to write to the file.
     * 
     * @throws IllegalSourcePathException
     *             If path is not a file.
     * @throws InvalidOpenOptionsException
     *             If an invalid combination of OpenOptions was provided.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    OutputStream newOutputStream(Path path, OpenOption... options) throws XenonException;

    /**
     * Get the {@link FileAttributes} of an existing path.
     * 
     * @param path
     *            the existing path.
     * 
     * @return the FileAttributes of the path.
     * 
     * @throws NoSuchPathException
     *             If a file does not exists.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    FileAttributes getAttributes(Path path) throws XenonException;

    /**
     * Reads the target of a symbolic link (optional operation).
     * 
     * @param link
     *            the link to read.
     * 
     * @return a Path representing the target of the link.
     * 
     * @throws NoSuchPathException
     *             If the link does not exists.
     * @throws IllegalSourcePathException
     *             If the source is not a link.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    Path readSymbolicLink(Path link) throws XenonException;

    /**
     * Sets the POSIX permissions of a path.
     * 
     * @param path
     *            the target path.
     * @param permissions
     *            the permissions to set.
     * 
     * @throws NoSuchPathException
     *             If the target path does not exists.
     * @throws XenonException
     *             If an I/O error occurred.
     */
    void setPosixFilePermissions(Path path, Set<PosixFilePermission> permissions) throws XenonException;

}
