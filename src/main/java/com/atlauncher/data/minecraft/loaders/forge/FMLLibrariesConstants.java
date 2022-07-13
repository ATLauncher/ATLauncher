/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
package com.atlauncher.data.minecraft.loaders.forge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FMLLibrariesConstants {
    private final static List<FMLLibrary> libs13 = List.of(
            new FMLLibrary("argo-2.25.jar", "bb672829fde76cb163004752b86b0484bd0a7f4b", 123642L),
            new FMLLibrary("guava-12.0.1.jar", "b8e78b9af7bf45900e14c6f958486b6ca682195f", 1795932L),
            new FMLLibrary("asm-all-4.0.jar", "98308890597acb64047f7e896638e0d98753ae82", 212767L));

    private final static List<FMLLibrary> libs14 = List.of(
            new FMLLibrary("argo-2.25.jar", "bb672829fde76cb163004752b86b0484bd0a7f4b", 123642L),
            new FMLLibrary("guava-12.0.1.jar", "b8e78b9af7bf45900e14c6f958486b6ca682195f", 1795932L),
            new FMLLibrary("asm-all-4.0.jar", "98308890597acb64047f7e896638e0d98753ae82", 212767L),
            new FMLLibrary("bcprov-jdk15on-147.jar", "b6f5d9926b0afbde9f4dbe3db88c5247be7794bb", 1997327L));

    private final static List<FMLLibrary> libs15 = List.of(
            new FMLLibrary("argo-small-3.2.jar", "58912ea2858d168c50781f956fa5b59f0f7c6b51", 91333L),
            new FMLLibrary("guava-14.0-rc3.jar", "931ae21fa8014c3ce686aaa621eae565fefb1a6a", 2189140L),
            new FMLLibrary("asm-all-4.1.jar", "054986e962b88d8660ae4566475658469595ef58", 214592L),
            new FMLLibrary("bcprov-jdk15on-148.jar", "960dea7c9181ba0b17e8bab0c06a43f0a5f04e65", 2318161L),
            new FMLLibrary("deobfuscation_data_1.5.zip", "5f7c142d53776f16304c0bbe10542014abad6af8", 200547L),
            new FMLLibrary("scala-library.jar", "458d046151ad179c85429ed7420ffb1eaf6ddf85", 7114640L));

    private final static List<FMLLibrary> libs151 = List.of(
            new FMLLibrary("argo-small-3.2.jar", "58912ea2858d168c50781f956fa5b59f0f7c6b51", 91333L),
            new FMLLibrary("guava-14.0-rc3.jar", "931ae21fa8014c3ce686aaa621eae565fefb1a6a", 2189140L),
            new FMLLibrary("asm-all-4.1.jar", "054986e962b88d8660ae4566475658469595ef58", 214592L),
            new FMLLibrary("bcprov-jdk15on-148.jar", "960dea7c9181ba0b17e8bab0c06a43f0a5f04e65", 2318161L),
            new FMLLibrary("deobfuscation_data_1.5.1.zip", "22e221a0d89516c1f721d6cab056a7e37471d0a6", 200886L),
            new FMLLibrary("scala-library.jar", "458d046151ad179c85429ed7420ffb1eaf6ddf85", 7114640L));

    private final static List<FMLLibrary> libs152 = List.of(
            new FMLLibrary("argo-small-3.2.jar", "58912ea2858d168c50781f956fa5b59f0f7c6b51", 91333L),
            new FMLLibrary("guava-14.0-rc3.jar", "931ae21fa8014c3ce686aaa621eae565fefb1a6a", 2189140L),
            new FMLLibrary("asm-all-4.1.jar", "054986e962b88d8660ae4566475658469595ef58", 214592L),
            new FMLLibrary("bcprov-jdk15on-148.jar", "960dea7c9181ba0b17e8bab0c06a43f0a5f04e65", 2318161L),
            new FMLLibrary("deobfuscation_data_1.5.2.zip", "446e55cd986582c70fcf12cb27bc00114c5adfd9", 201404L),
            new FMLLibrary("scala-library.jar", "458d046151ad179c85429ed7420ffb1eaf6ddf85", 7114640L));

    public final static Map<String, List<FMLLibrary>> fmlLibraries = new HashMap<>();

    static {
        fmlLibraries.put("1.3.2", libs13);
        fmlLibraries.put("1.4", libs14);
        fmlLibraries.put("1.4.1", libs14);
        fmlLibraries.put("1.4.2", libs14);
        fmlLibraries.put("1.4.3", libs14);
        fmlLibraries.put("1.4.4", libs14);
        fmlLibraries.put("1.4.5", libs14);
        fmlLibraries.put("1.4.6", libs14);
        fmlLibraries.put("1.4.7", libs14);
        fmlLibraries.put("1.5", libs15);
        fmlLibraries.put("1.5.1", libs151);
        fmlLibraries.put("1.5.2", libs152);
    }
}
