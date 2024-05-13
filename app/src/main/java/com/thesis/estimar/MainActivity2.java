package com.thesis.estimar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import org.tensorflow.lite.Interpreter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.graphics.Typeface;



public class MainActivity2 extends AppCompatActivity {

    EditText roomsEditText,engEditText, projectEditText, locationEditText, windowsEditText, doorsEditText, f1EditText, f1_1EditText, f1_2EditText, f2_1EditText, f2_2EditText, f3_2EditText;
    Spinner floorsSpinner, windowtypeSpinner, doortypeSpinner, ceilingtypeSpinner, wallingtypeSpinner, flooringtypeSpinner;
    Button calculateButton, clearButton, resetButton, showMoreButton;
    TextView totalCostTextView, laborCostTextView, cementCostTextView, sandCostTextView, gravelCostTextView, chbCostTextView, otherCostTextView, calculationHistoryTextView, cementQuantityTextView, sandQuantityTextView, chbQuantityTextView, gravelQuantityTextView;
    List<String> calculationHistory = new ArrayList<>();
    Interpreter interpreter;

    private DatabaseHelper dbHelper;

    private String originalLocation;
    private String originalAreaF1;
    private String originalAreaF11;
    private String originalAreaF12;
    private String originalAreaF21;
    private String originalAreaF22;
    private String originalAreaF32;
    private String originalProject;
    private String originalRooms;
    private String originalWindows;
    private String originalDoors;
    private String calculationResult;
    private String calculationResult1;

    @SuppressLint({"CutPasteId", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Initialize UI components
        projectEditText = findViewById(R.id.projectEditText);
        engEditText = findViewById(R.id.engEditText);
        locationEditText = findViewById(R.id.locationEditText);
        f1EditText = findViewById(R.id.f1EditText);
        f1_1EditText = findViewById(R.id.f1_1EditText);
        f1_2EditText = findViewById(R.id.f1_2EditText);
        f2_1EditText = findViewById(R.id.f2_1EditText);
        f2_2EditText = findViewById(R.id.f2_2EditText);
        f3_2EditText = findViewById(R.id.f3_2EditText);
        roomsEditText = findViewById(R.id.rooms);
        windowsEditText = findViewById(R.id.windows);
        doorsEditText = findViewById(R.id.doors);
        floorsSpinner = findViewById(R.id.floorsSpinner);
        calculateButton = findViewById(R.id.calculateButton);
        totalCostTextView = findViewById(R.id.totalCostTextView);
        clearButton = findViewById(R.id.clear);
        windowtypeSpinner = findViewById(R.id.windowtypeSpinner);
        doortypeSpinner = findViewById(R.id.doorTypeSpinner);
        resetButton = findViewById(R.id.reset);
        ceilingtypeSpinner = findViewById(R.id.ceilingtypeSpinner);
        wallingtypeSpinner = findViewById(R.id.wallingtypeSpinner);
        flooringtypeSpinner = findViewById(R.id.flooringtypeSpinner);
        showMoreButton = findViewById(R.id.showmore);
        dbHelper = new DatabaseHelper(this);


        //calculate button
        calculateButton.setOnClickListener(v -> calculateCost());
        //clear button
        clearButton.setOnClickListener(v -> clearInputs());

        //reset button
        resetButton.setOnClickListener(v -> resetInputs());

        //show more button
        showMoreButton.setOnClickListener(v -> showDialog());

        // Load TensorFlow Lite model
        try {
            interpreter = new Interpreter(loadModelFile(), null);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        //floorsSpinner item selection listener
        floorsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                String numericPart = selectedItem.split(" ")[0];
                int selectedFloors = Integer.parseInt(numericPart);
                handleFloorEditTextVisibility(selectedFloors);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // default visibility for floor EditText fields
        handleFloorEditTextVisibility(1); // Default: 1 floor
    }

    // Method to handle visibility of floor EditText fields
    private void handleFloorEditTextVisibility(int selectedFloors) {
        switch (selectedFloors) {
            case 1:
                f1EditText.setVisibility(View.VISIBLE);
                f1_1EditText.setVisibility(View.INVISIBLE);
                f1_2EditText.setVisibility(View.INVISIBLE);
                f2_1EditText.setVisibility(View.INVISIBLE);
                f2_2EditText.setVisibility(View.INVISIBLE);
                f3_2EditText.setVisibility(View.INVISIBLE);
                break;
            case 2:
                f1EditText.setVisibility(View.INVISIBLE);
                f1_1EditText.setVisibility(View.VISIBLE);
                f1_2EditText.setVisibility(View.INVISIBLE);
                f2_1EditText.setVisibility(View.VISIBLE);
                f2_2EditText.setVisibility(View.INVISIBLE);
                f3_2EditText.setVisibility(View.INVISIBLE);
                break;
            case 3:
                f1EditText.setVisibility(View.INVISIBLE);
                f1_1EditText.setVisibility(View.INVISIBLE);
                f1_2EditText.setVisibility(View.VISIBLE);
                f2_1EditText.setVisibility(View.INVISIBLE);
                f2_2EditText.setVisibility(View.VISIBLE);
                f3_2EditText.setVisibility(View.VISIBLE);
                break;
            default:

                break;
        }
    }


    private MappedByteBuffer loadModelFile() throws IOException {
        AssetManager assetManager = getAssets();
        AssetFileDescriptor fileDescriptor = assetManager.openFd("random_forest_2.tflite");
        try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private void clearInputs() {
        // Store original inputs
        originalProject = projectEditText.getText().toString();
        originalLocation = locationEditText.getText().toString();
        originalAreaF1 = f1EditText.getText().toString();
        originalAreaF11 = f1_1EditText.getText().toString();
        originalAreaF12 = f1_2EditText.getText().toString();
        originalAreaF21 = f2_1EditText.getText().toString();
        originalAreaF22 = f2_2EditText.getText().toString();
        originalAreaF32 = f3_2EditText.getText().toString();
        originalRooms = roomsEditText.getText().toString();
        originalWindows = windowsEditText.getText().toString();
        originalDoors = doorsEditText.getText().toString();

        // Clear input fields
        projectEditText.setText("");
        locationEditText.setText("");
        f1EditText.setText("");
        f1_1EditText.setText("");
        f1_2EditText.setText("");
        f2_1EditText.setText("");
        f2_2EditText.setText("");
        f3_2EditText.setText("");
        roomsEditText.setText("");
        windowsEditText.setText("");
        doorsEditText.setText("");
    }

    private void resetInputs() {
        // Restore original inputs
        projectEditText.setText(originalProject);
        locationEditText.setText(originalLocation);
        f1EditText.setText(originalAreaF1);
        f1_1EditText.setText(originalAreaF11);
        f1_2EditText.setText(originalAreaF12);
        f2_1EditText.setText(originalAreaF21);
        f2_2EditText.setText(originalAreaF22);
        f3_2EditText.setText(originalAreaF32);
        roomsEditText.setText(originalRooms);
        windowsEditText.setText(originalWindows);
        doorsEditText.setText(originalDoors);

        Toast.makeText(this, "Inputs restored", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void calculateCost() {
        try {

            double area = 0.0;
            int floors;


            String selectedFloors = floorsSpinner.getSelectedItem().toString();

            String numericPart = selectedFloors.split(" ")[0];
            floors = Integer.parseInt(numericPart);


            switch (floors) {
                case 1:
                    area = Double.parseDouble(f1EditText.getText().toString());
                    break;
                case 2:
                    area = Double.parseDouble(f1_1EditText.getText().toString()) +
                            Double.parseDouble(f2_1EditText.getText().toString());
                    break;
                case 3:
                    area = Double.parseDouble(f1_2EditText.getText().toString()) +
                            Double.parseDouble(f2_2EditText.getText().toString()) +
                            Double.parseDouble(f3_2EditText.getText().toString());
                    break;
                default:

                    break;
            }


            int rooms = Integer.parseInt(roomsEditText.getText().toString());
            int windows = Integer.parseInt(windowsEditText.getText().toString());
            int window_type = getWindowTypeIndex(windowtypeSpinner.getSelectedItem().toString());
            int doors = Integer.parseInt(doorsEditText.getText().toString());
            int door_type = getDoorTypeIndex(doortypeSpinner.getSelectedItem().toString());
            int ceiling = getCeilingTypeIndex(ceilingtypeSpinner.getSelectedItem().toString());
            int wall_type = getWallTypeIndex(wallingtypeSpinner.getSelectedItem().toString());
            int flooring = getFloorTypeIndex(flooringtypeSpinner.getSelectedItem().toString());


            float[][] input = new float[1][20];
            input[0][0] = (float) area;
            input[0][1] = floors;
            input[0][2] = rooms;
            input[0][3] = windows;
            input[0][4] = doors;
            input[0][5] = flooring;
            input[0][6] = wall_type;
            input[0][7] = window_type;
            input[0][8] = door_type;
            input[0][9] = ceiling;

            float[][] output = new float[1][1];
            interpreter.run(input, output);

            // Run inference on the TensorFlow Lite model
            float totalCost = output[0][0];

            DecimalFormat decimalFormat = new DecimalFormat("#,###");

            String formattedTotalCost = decimalFormat.format(totalCost);

            totalCostTextView.setText("Total Cost: ₱" + formattedTotalCost);
            calculationResult = "Area: " + area + ", Floors: " + floors + ", Rooms: " + rooms + ", Windows Count: " + windows + ", Doors Count: " + doors + ", Total Cost: ₱" + formattedTotalCost;

        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            Log.e("CalculationError", "Error in calculateCost: " + e.getMessage(), e);
            totalCostTextView.setText("error during calculation.");
        }
    }


    private int getWindowTypeIndex(String window_type) {
        switch (window_type) {
            case "standard":
                return 1;
            case "full window":
                return 2;
            case "minimal":
            default:
                return 0;
        }
    }

    private int getDoorTypeIndex(String door_type) {
        switch (door_type) {
            case "panel door":
                return 1;
            case "uPVC door":
                return 2;
            case "aluminum door":
            default:
                return 0;
        }
    }

    private int getCeilingTypeIndex(String ceiling) {
        switch (ceiling) {
            case "gypsum":
                return 1;
            case "PVC":
                return 2;
            case "none":
            default:
                return 0;
        }
    }

    private int getWallTypeIndex(String wall_type) {
        switch (wall_type) {
            case "paint":
                return 1;
            case "cladding":
                return 2;
            case "concrete":
            default:
                return 0;
        }
    }

    private int getFloorTypeIndex(String flooring) {
        switch (flooring) {
            case "tiles":
                return 1;
            case "granite":
                return 2;
            case "concrete":
            default:
                return 0;
        }
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(MainActivity2.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_dialog);

        // Initialize UI components in the dialog
        cementCostTextView = dialog.findViewById(R.id.cementCostTextView);
        sandCostTextView = dialog.findViewById(R.id.sandCostTextView);
        gravelCostTextView = dialog.findViewById(R.id.gravelCostTextView);
        chbCostTextView = dialog.findViewById(R.id.chbCostTextView);
        laborCostTextView = dialog.findViewById(R.id.laborCostTextView);
        otherCostTextView = dialog.findViewById(R.id.otherCostTextView);
        calculationHistoryTextView = dialog.findViewById(R.id.calculationHistoryTextView);
        cementQuantityTextView = dialog.findViewById(R.id.cementQuantityTextView);
        sandQuantityTextView = dialog.findViewById(R.id.sandQuantityTextView);
        gravelQuantityTextView = dialog.findViewById(R.id.gravelQuantityTextView);
        chbQuantityTextView = dialog.findViewById(R.id.chbQuantityTextView);

        // Calculate costs again
        calculateCostDialog();

        // Show the dialog
        dialog.show();

        // Set dialog properties
        Window window = dialog.getWindow();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_bg);
        assert window != null;
        window.setGravity(Gravity.BOTTOM);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }


    @SuppressLint("SetTextI18n")
    private void calculateCostDialog() {
        try {

            double area = 0.0;
            int floors;


            String selectedFloors = floorsSpinner.getSelectedItem().toString();

            String numericPart = selectedFloors.split(" ")[0];
            floors = Integer.parseInt(numericPart);


            switch (floors) {
                case 1:
                    area = Double.parseDouble(f1EditText.getText().toString());
                    break;
                case 2:
                    area = Double.parseDouble(f1_1EditText.getText().toString()) +
                            Double.parseDouble(f2_1EditText.getText().toString());
                    break;
                case 3:
                    area = Double.parseDouble(f1_2EditText.getText().toString()) +
                            Double.parseDouble(f2_2EditText.getText().toString()) +
                            Double.parseDouble(f3_2EditText.getText().toString());
                    break;
                default:

                    break;
            }


            int rooms = Integer.parseInt(roomsEditText.getText().toString());
            int windows = Integer.parseInt(windowsEditText.getText().toString());
            int window_type = getWindowTypeIndex(windowtypeSpinner.getSelectedItem().toString());
            int doors = Integer.parseInt(doorsEditText.getText().toString());
            int door_type = getDoorTypeIndex(doortypeSpinner.getSelectedItem().toString());
            int ceiling = getCeilingTypeIndex(ceilingtypeSpinner.getSelectedItem().toString());
            int wall_type = getWallTypeIndex(wallingtypeSpinner.getSelectedItem().toString());
            int flooring = getFloorTypeIndex(flooringtypeSpinner.getSelectedItem().toString());


            float[][] input = new float[1][20];
            input[0][0] = (float) area;
            input[0][1] = floors;
            input[0][2] = rooms;
            input[0][3] = windows;
            input[0][4] = doors;
            input[0][5] = flooring;
            input[0][6] = wall_type;
            input[0][7] = window_type;
            input[0][8] = door_type;
            input[0][9] = ceiling;

            float[][] output = new float[1][1];
            interpreter.run(input, output);

            float totalCost = output[0][0];

            float cementCost = totalCost * 0.1024f;

            float sandCost = totalCost * 0.02123f;

            float gravelCost = totalCost * 0.02736f;

            float chbCost = totalCost * 0.02916f;

            float laborCost = totalCost * 0.35f;

            float otherCost = totalCost * 0.46985f;

            //quantity(updatable prices based on the market)
            float cementQuant = cementCost / 260;
            float sandQuant = sandCost / 1430;
            float gravelQuant = gravelCost / 1300;
            float chbQuant = chbCost / 15;

            DecimalFormat decimalFormat = new DecimalFormat("#,###");

            String formattedCementCost = decimalFormat.format(cementCost);
            String formattedSandCost = decimalFormat.format(sandCost);
            String formattedGravelCost = decimalFormat.format(gravelCost);
            String formattedCHBCost = decimalFormat.format(chbCost);
            String formattedOtherCost = decimalFormat.format(otherCost);
            String formattedCementQuant = decimalFormat.format(cementQuant);
            String formattedSandQuant = decimalFormat.format(sandQuant);
            String formattedGravelQuant = decimalFormat.format(gravelQuant);
            String formattedChbQuant = decimalFormat.format(chbQuant);
            String formattedLaborCost = decimalFormat.format(laborCost);


            // Set text for TextViews in the dialog
            cementCostTextView.setText("Cement Cost: ₱" + formattedCementCost);
            sandCostTextView.setText("Sand Cost: ₱" + formattedSandCost);
            gravelCostTextView.setText("Gravel Cost: ₱" + formattedGravelCost);
            chbCostTextView.setText("CHB Cost: ₱" + formattedCHBCost);
            laborCostTextView.setText("Labor Cost: ₱" + formattedLaborCost);
            otherCostTextView.setText("Other Costs: ₱" + formattedOtherCost);
            cementQuantityTextView.setText("Cement quantity:" + formattedCementQuant + "bags");
            sandQuantityTextView.setText("Sand quantity:" + formattedSandQuant + "cu m.");
            gravelQuantityTextView.setText("Gravel quantity:" + formattedGravelQuant + "cu m.");
            chbQuantityTextView.setText("CHB quantity:" + formattedChbQuant + "pieces");


            calculationResult1 = "Cement: ₱" + formattedCementCost + ", Sand: ₱" + formattedSandCost + ", Gravel: ₱" + formattedGravelCost + ", CHB: ₱" + formattedCHBCost + ", Labor: ₱" + formattedLaborCost + ", Others: ₱" + formattedOtherCost + ", cement quant:" + formattedCementQuant + ", sand quant:" + formattedSandQuant + ", gravel quant:" + formattedGravelQuant + ", chb quant:" + formattedChbQuant;


        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            Log.e("CalculationError", "Error in calculateCostDialog: " + e.getMessage(), e);
            totalCostTextView.setText("error during calculation.");
        }
        calculationHistory.add(calculationResult);
        calculationHistory.add(calculationResult1);

        // Update the calculation history TextView
        updateCalculationHistory();
        dbHelper.addCalculation(calculationResult);
        dbHelper.addCalculation(calculationResult1);
    }

    private void updateCalculationHistory() {
        StringBuilder historyText = new StringBuilder("\n");
        for (String calculation : calculationHistory) {
            historyText.append(calculation).append("\n");
        }
        calculationHistoryTextView.setText(historyText.toString());
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void downloadPDF(View view) {
        createPDFFromDatabase();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void createPDFFromDatabase() {
        String eng = engEditText.getText().toString().trim();
        String project = projectEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();


        if (project.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please enter project and location information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a file name for the PDF
        String fileName = project + "_" + location + "_cost_estimate.pdf";

        // Create PDF content
        StringBuilder content = new StringBuilder();
        content.append("<b>").append("Project Title: ").append("</b>").append(project).append("\n");
        content.append("<i>").append("Location: ").append("</i>").append(location).append("\n\n");


        content.append("<b>").append("Inputs").append("</b>").append("\n");
        int floors = Integer.parseInt(floorsSpinner.getSelectedItem().toString().split(" ")[0]);
        switch (floors) {
            case 1:
                content.append("Area (Floor 1):\t").append(f1EditText.getText().toString()).append("\n");
                break;
            case 2:
                content.append("Area (Floor 1):\t").append(f1_1EditText.getText().toString()).append("\n");
                content.append("Area (Floor 2):\t").append(f2_1EditText.getText().toString()).append("\n");
                break;
            case 3:
                content.append("Area (Floor 1):\t").append(f1_2EditText.getText().toString()).append("\n");
                content.append("Area (Floor 2):\t").append(f2_2EditText.getText().toString()).append("\n");
                content.append("Area (Floor 3):\t").append(f3_2EditText.getText().toString()).append("\n");
                break;
            default:
                break;
        }
        content.append("Rooms:\t").append(roomsEditText.getText().toString()).append("\n");
        content.append("Windows:\t").append(windowsEditText.getText().toString()).append("\n");
        content.append("Window Type:\t").append(windowtypeSpinner.getSelectedItem().toString()).append("\n");
        content.append("Doors:\t").append(doorsEditText.getText().toString()).append("\n");
        content.append("Door Type:\t").append(doortypeSpinner.getSelectedItem().toString()).append("\n");
        content.append("Ceiling Type:\t").append(ceilingtypeSpinner.getSelectedItem().toString()).append("\n");
        content.append("Wall Type:\t").append(wallingtypeSpinner.getSelectedItem().toString()).append("\n");
        content.append("Floor Type:\t").append(flooringtypeSpinner.getSelectedItem().toString()).append("\n\n");


        content.append("<b>").append("Results").append("</b>").append("\n");



        if (cementCostTextView != null && sandCostTextView != null && gravelCostTextView != null && chbCostTextView != null &&
                laborCostTextView != null && otherCostTextView != null && cementQuantityTextView != null && sandQuantityTextView != null &&
                gravelQuantityTextView != null && chbQuantityTextView != null) {

            content.append("\t").append(cementCostTextView.getText().toString()).append("\n");
            content.append("\t").append(cementQuantityTextView.getText().toString()).append("\n");
            content.append("\t").append(sandCostTextView.getText().toString()).append("\n");
            content.append("\t").append(sandQuantityTextView.getText().toString()).append("\n");
            content.append("\t").append(gravelCostTextView.getText().toString()).append("\n");
            content.append("\t").append(gravelQuantityTextView.getText().toString()).append("\n");
            content.append("\t").append(chbCostTextView.getText().toString()).append("\n");
            content.append("\t").append(chbQuantityTextView.getText().toString()).append("\n");
            content.append("\t").append(laborCostTextView.getText().toString()).append("\n");
            content.append("\t").append(otherCostTextView.getText().toString()).append("\n");

        }
        content.append("<b>").append(totalCostTextView.getText().toString()).append("<b>").append("\n");
        content.append("<b>").append("").append("</b>").append("\n");
        content.append("<b>").append("").append("</b>").append("\n");
        content.append("Noted By:").append("\n");
        content.append("<b>").append("").append("</b>").append("\n");
        content.append("<b>").append("").append("</b>").append("\n");
        content.append("<b>").append("").append("</b>").append(eng).append("\n");


        try {
            File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            FileOutputStream fos = new FileOutputStream(pdfFile);
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(612, 792, 1).create(); // 8.5x11 inches
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(12);
            paint.setTypeface(Typeface.create("Verdana", Typeface.NORMAL)); // Verdana font
            float textX = 72; // 1 inch margin (1 inch = 72 points)
            float textY = 72; // 1 inch margin (1 inch = 72 points)

            for (String line : content.toString().split("\n")) {
                if (line.startsWith("<b>") || line.startsWith("<i>")) {
                    paint.setFakeBoldText(line.startsWith("<b>"));
                    paint.setTextSkewX(line.startsWith("<i>") ? -0.25f : 0);
                    line = line.replace("<b>", "").replace("</b>", "").replace("<i>", "").replace("</i>", "");
                } else {
                    paint.setFakeBoldText(false);
                    paint.setTextSkewX(0);
                }
                canvas.drawText(line, textX, textY, paint);
                textY += paint.descent() - paint.ascent();
            }

            pdfDocument.finishPage(page);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();
            Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}