
# README — Importing the Eclipse Project (JavaFX)  

This guide shows how to **Import** the `CountingGame` Eclipse project and what you **STILL NEED TO** do so it runs reliably with JavaFX.

> Target setup used in examples:  
> **JDK:** `C:\Java\Oracle_JDK-24`  
> **JavaFX SDK:** `C:\javafx-sdk-24.0.2`



## 1) Import the existing Eclipse project
**File → Import… → General → Existing Projects into Workspace → Next**  
- **Select root directory**: choose the folder that contains the project’s `.project` file  
- Check the project → **Finish**

> ✅ Import completes, but you **STILL NEED TO** do the steps below.

 

## 2) STILL NEED TO — Point Eclipse at your **JDK 24**
1. **Window → Preferences → Java → Installed JREs → Add… → Standard VM**
2. **JRE home:** `C:\Java\Oracle_JDK-24`
3. Name it `Oracle_JDK-24` → **Finish**  
4. Check it as the **default** → **Apply and Close**

 

## 3) STILL NEED TO — Put JavaFX **on the Modulepath** (compile‑time)
1. **Project → Properties → Java Build Path → Libraries**
2. **Add Library… → User Library → Next → User Libraries… → New…**
   - Name it: `JavaFX24_24.0.2`
   - With it selected → **Add External JARs…** → select **all** jars from  
     `C:\javafx-sdk-24.0.2\lib\`
   - OK → Close
3. Back in **Add Library…** select `JavaFX24_24.0.2` → **Finish**
4. Confirm the entry appears under **Modulepath** (not Classpath).  
   If it landed under Classpath, **remove** and re‑add it.

 

## 4) STILL NEED TO — Create the **Run Configuration** (runtime flags)
1. **Run → Run Configurations… → Java Application → New launch configuration**
2. **Project:** `CountingGame`
3. **Main class:** `countinggame.CountingGame` (use **Search…** if needed)
4. **Arguments → VM arguments** (single line; no duplicates):
```
--module-path "C:\javafx-sdk-24.0.2\lib" --add-modules javafx.controls,javafx.graphics,javafx.media --enable-native-access=javafx.graphics
```
> Tip: Use either `--add-modules X,Y` **or** `--add-modules=X,Y`. Don’t include both forms.  
> Do **not** duplicate `--module-path`. No trailing dot (`.`).

 

## 5) STILL NEED TO — Make sure **resources** are on the classpath
The code searches these classpath locations (first match wins):
```
/countinggame/resources/
/countinggame/
/resources/
/
 

### Preferred layout (easiest to get right)
 
CountingGame/
  src/
    countinggame/
      CountingGame.java
      resources/
        bluesparklesbackground.png
        1.png … 10.png
        1.mp3 … 10.mp3
 
This compiles to `/countinggame/resources/...` and will be found automatically.

### Alternative layout
Create a top‑level `resources/` folder → **Right‑click → Build Path → Use as Source Folder** → put the files inside.

**After placing files:** **Refresh (F5)** the project → **Project → Clean…**



## 6) STILL NEED TO — Verify package and names
- First line of `CountingGame.java` must be exactly:
 java
  package countinggame;
 
- File name must be **CountingGame.java** (matches `public class CountingGame`)



## 7) STILL NEED TO — Ensure Eclipse builds to **bin/**
- Turn on **Project → Build Automatically**
- **Project → Clean…** → select `CountingGame` → **Clean**
- Check that `bin/` appears with compiled classes:

CountingGame/
├─ bin/
│  └─ countinggame/
│     ├─ CountingGame.class
│     └─ resources/   (if you used src/countinggame/resources)
│        ├─ bluesparklesbackground.png
│        ├─ 1.png … 10.png
│        └─ 1.mp3 … 10.mp3
└─ src/
   └─ countinggame/
      ├─ CountingGame.java
      └─ resources/
         ├─ bluesparklesbackground.png
         ├─ 1.png … 10.png
         └─ 1.mp3 … 10.mp3


> If `bin/` is missing: **Project → Properties → Java Build Path → Source** → verify **Default output folder** is `CountingGame/bin`. Adjust if necessary.



## Optional — `module-info.java` (JPMS)
If you want a module descriptor, add this at the project root:
```java
module CountingGame {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    exports countinggame;
}


You can keep the VM arguments even with JPMS; it’s simple and works well in Eclipse.


## Common errors & quick fixes

### “import javafx … cannot be resolved”
- JavaFX SDK jars weren’t added to the **Modulepath** (Step 3). Fix it and **Clean**.

### “Could not find or load main class countinggame.CountingGame”
- Wrong package line or main class in Run Config. Fix the `package` line, set **Project/Main class**, **Clean**.

### Images / Sounds not found
- Put assets under `src/countinggame/resources` (preferred), **Refresh**, then **Clean**.  
- Filenames must match exactly: `1.png`, `1.mp3`, etc.

### EGit “HOME not set”
- Set an environment variable: `HOME=C:\Users\<YourUser>` (optional; unrelated to JavaFX).

### Renderer “Unsafe” warnings
- From Marlin renderer; harmless for this app.



## VM arguments (copy‑paste)

--module-path "C:\javafx-sdk-24.0.2\lib" --add-modules javafx.controls,javafx.graphics,javafx.media --enable-native-access=javafx.graphics


All set! Import the project, complete the **STILL NEED TO** items, then **Run As → Java Application** on `CountingGame.java`.
