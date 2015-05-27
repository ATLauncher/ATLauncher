/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.data.json;

import com.atlauncher.annot.Json;

<<<<<<< HEAD
import java.util.LinkedList;
=======
import java.util.ArrayList;
>>>>>>> 52af8025f1779074f588db77f601f5449a2f3e82
import java.util.List;

@Json
public class Deletes {
<<<<<<< HEAD
    private List<Delete> files = new LinkedList<>();
    private List<Delete> folders = new LinkedList<>();
=======
    private List<Delete> files = new ArrayList<>();
    private List<Delete> folders = new ArrayList<>();
>>>>>>> 52af8025f1779074f588db77f601f5449a2f3e82

    public List<Delete> getFiles() {
        return this.files;
    }

    public List<Delete> getFolders() {
        return this.folders;
    }
}