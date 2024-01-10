package com.example.apptipcal

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.Switch
import android.widget.Spinner
import android.widget.TextView
import android.widget.EditText
import kotlin.math.roundToInt
import androidx.appcompat.app.AppCompatDelegate


class MainActivity : AppCompatActivity() {

    private lateinit var billAmountEditText: EditText
    private lateinit var tipPercentageRadioGroup: RadioGroup
    private lateinit var customTipRadioButton: RadioButton
    private lateinit var customTipEditText: EditText
    private lateinit var currencySpinner: Spinner
    private lateinit var selectCurrencyTextView: TextView
    private lateinit var calculateButton: Button
    private lateinit var tipAmountTextView: TextView
    private lateinit var totalBillTextView: TextView
    private lateinit var roundTipSwitch: Switch
    private lateinit var splitBillSwitch: Switch
    private lateinit var numberOfPeopleEditText: EditText
    private lateinit var selectedCurrency: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set the title
        supportActionBar?.title = "Tip Calculator"

        // Access the Switches
        roundTipSwitch = findViewById(R.id.roundTipSwitch)
        splitBillSwitch = findViewById(R.id.splitBillSwitch)
        numberOfPeopleEditText = findViewById(R.id.numberOfPeopleEditText)

        // Access the RadioGroup and RadioButton for custom tip
        tipPercentageRadioGroup = findViewById(R.id.tipPercentageRadioGroup)
        customTipRadioButton = findViewById(R.id.radioCustom)

        // Access the EditTexts for custom tip amount and number of people
        customTipEditText = findViewById(R.id.customTipEditText)

        billAmountEditText = findViewById(R.id.billAmountEditText)
        currencySpinner = findViewById(R.id.currencySpinner)
        selectCurrencyTextView = findViewById(R.id.selectCurrencyTextView)
        calculateButton = findViewById(R.id.calculateButton)
        tipAmountTextView = findViewById(R.id.tipAmountTextView)
        totalBillTextView = findViewById(R.id.totalBillTextView)

        // Populate Spinner with currency options
        val currencies = arrayOf("USD", "EUR", "GBP", "JPY", "CAD", "LKR") // Added LKR
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter

        // Set default currency
        selectedCurrency = currencies[0]

        // Add a listener to handle currency selection
        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                selectedCurrency = currencies[position]
                // Update the displayed currency
                selectCurrencyTextView.text = "Selected Currency: $selectedCurrency"
                // Reset input fields when currency changes
                resetInputFields()
                // Recalculate tip when currency changes
                calculateTip()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Handle when nothing is selected
            }
        }

        // Set up a listener for the RadioGroup
        tipPercentageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            // Handle the selected radio button
            when (checkedId) {
                R.id.radioCustom -> {
                    // Show the custom tip EditText when "Custom" is selected
                    customTipEditText.visibility = View.VISIBLE
                }
                else -> {
                    // Hide the custom tip EditText for other options
                    customTipEditText.visibility = View.GONE
                    // Update the tip calculation for other options
                    calculateTip()
                }
            }
        }

        // Set up a listener for the Switches
        roundTipSwitch.setOnCheckedChangeListener { _, _ ->
            // Handle the switch state
            calculateTip()
        }

        splitBillSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle the switch state
            updateNumberOfPeopleVisibility()
            calculateTip()
            if (isChecked) {
                // Show additional details or perform any other action when split bill is enabled
            }
        }

        calculateButton.setOnClickListener {
            // Check for empty or invalid bill amount
            if (billAmountEditText.text.isNullOrBlank() || billAmountEditText.text.toString().toDoubleOrNull() == null) {
                showInvalidAmountPopup()
            } else {
                // Moved the result popup to here
                showResultPopup()
            }
        }

        // Initialize Exit Button
        val exitButton: Button = findViewById(R.id.exitButton)
        exitButton.setOnClickListener {
            finish() // Finish the activity, closing the app
        }
    }


    // Function to update the visibility of the number of people EditText based on the Split Bill switch state
    private fun updateNumberOfPeopleVisibility() {
        val visibility = if (splitBillSwitch.isChecked) View.VISIBLE else View.INVISIBLE
        numberOfPeopleEditText.visibility = visibility
    }

    private fun getSelectedTipPercentage(): Double {
        val tipPercentageArray = arrayOf(5, 10, 15, 20)

        val checkedRadioButtonIndex = tipPercentageRadioGroup.indexOfChild(findViewById(tipPercentageRadioGroup.checkedRadioButtonId))

        if (checkedRadioButtonIndex == -1) {
            return 0.0
        }

        val selectedPercentage = tipPercentageArray[checkedRadioButtonIndex]
        return selectedPercentage.toDouble()
    }

    private fun roundOffTip(tipAmount: Double): Double {
        return (tipAmount * 100).roundToInt() / 100.0
    }

    private fun getCurrencySymbol(currencyCode: String): String {
        val currency = java.util.Currency.getInstance(currencyCode)
        return currency.symbol
    }

    private fun formatCurrency(amount: Double, currencyCode: String): String {
        val currencyFormat = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.getDefault())
        currencyFormat.currency = java.util.Currency.getInstance(currencyCode)
        return currencyFormat.format(amount)
    }

    private fun updateTipCalculation() {
        try {
            val tipPercentage: Double = getSelectedTipPercentage()

            val isRoundTipEnabled: Boolean = roundTipSwitch.isChecked
            val isSplitBillEnabled: Boolean = splitBillSwitch.isChecked

            val billAmount: Double = billAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
            var tipAmount: Double = billAmount * (tipPercentage / 100)

            if (isRoundTipEnabled) {
                tipAmount = roundOffTip(tipAmount)
            }

            var totalBill: Double = billAmount + tipAmount
            if (isSplitBillEnabled) {
                val numberOfPeople: Int = numberOfPeopleEditText.text.toString().toIntOrNull() ?: 1
                if (numberOfPeople > 0) {
                    totalBill /= numberOfPeople
                }
            }

            val currencySymbol = getCurrencySymbol(selectedCurrency)
            val formattedTipAmount = formatCurrency(tipAmount, selectedCurrency)
            val formattedTotalBill = formatCurrency(totalBill, selectedCurrency)

            tipAmountTextView.text = getString(R.string.tip_amount_with_currency, formattedTipAmount, currencySymbol)
            totalBillTextView.text = getString(R.string.total_bill_with_currency_bold, formattedTotalBill, currencySymbol)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateTip() {
        val billAmount = billAmountEditText.text.toString().toDoubleOrNull() ?: 0.0

        val tipPercentage = when {
            tipPercentageRadioGroup.checkedRadioButtonId == R.id.radioCustom -> customTipEditText.text.toString().toDoubleOrNull() ?: 0.0
            else -> getSelectedTipPercentage()
        }

        var tipAmount = (billAmount * tipPercentage) / 100

        if (roundTipSwitch.isChecked) {
            tipAmount = roundOffTip(tipAmount)
        }

        var totalBill = billAmount + tipAmount
        if (splitBillSwitch.isChecked) {
            val numberOfPeople = numberOfPeopleEditText.text.toString().toIntOrNull() ?: 1
            if (numberOfPeople > 0) {
                totalBill /= numberOfPeople
            }
        }

        val currencySymbol = getCurrencySymbol(selectedCurrency)
        val formattedTipAmount = formatCurrency(tipAmount, selectedCurrency)
        val formattedTotalBill = formatCurrency(totalBill, selectedCurrency)

        tipAmountTextView.text = getString(R.string.tip_amount_with_currency, formattedTipAmount, currencySymbol)
        totalBillTextView.text = getString(R.string.total_bill_with_currency_bold, formattedTotalBill, currencySymbol)
    }

    private fun showResultPopup() {
        val billAmount = billAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val tipAmount = (billAmount * getSelectedTipPercentage()) / 100

        val isSplitBillEnabled: Boolean = splitBillSwitch.isChecked
        val numberOfPeople = numberOfPeopleEditText.text.toString()

        val message = if (isSplitBillEnabled) {
            "Number of People Paying the Bill Amount: $numberOfPeople\nPayment Per One Person: ${(totalBillTextView.text.toString().toDoubleOrNull() ?: 0.0)}"
        } else {
            "Bill Amount: $billAmount\nTip Amount: $tipAmount"
        }

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Result")
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { _, _ -> }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showInvalidAmountPopup() {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Invalid Amount")
        alertDialogBuilder.setMessage("Please enter a valid bill amount.")
        alertDialogBuilder.setPositiveButton("OK") { _, _ -> }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun resetInputFields() {
        billAmountEditText.text.clear()
        customTipEditText.text.clear()
        numberOfPeopleEditText.text.clear()
    }
}
