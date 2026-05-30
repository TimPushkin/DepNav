/**
 * DepNav -- department navigator.
 * Copyright (C) 2026  Timofei Pushkin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * For cases when [org.gradle.api.tasks.Copy] cannot be used because lazy properties are required.
 */
abstract class CopyFilesTask @Inject constructor(
    private val fs: FileSystemOperations
) : DefaultTask() {
    @get:InputFiles
    abstract val sources: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    @TaskAction
    @Suppress("UndocumentedPublicFunction") // Described by class'es doc
    fun copy() {
        fs.copy {
            from(sources)
            into(destination)
        }
    }
}
