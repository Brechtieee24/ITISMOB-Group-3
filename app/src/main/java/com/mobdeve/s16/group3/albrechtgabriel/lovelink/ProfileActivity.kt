package com.mobdeve.s16.group3.albrechtgabriel.lovelink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s16.group3.albrechtgabriel.lovelink.databinding.ProfilePageBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ProfilePageBinding
    private lateinit var activityAdapter: ProfileActivityAdapter
    private lateinit var activityList: ArrayList<String>
    private lateinit var monthlyResidencyAdapter: MonthlyResidencyAdapter
    private lateinit var monthlyResidencyList: ArrayList<MonthlyResidency>

    private var originalBioText: String = ""
    private var selectedImageUri: Uri? = null

    // Photo picker launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.profilePictureIv.setImageURI(it)
            Toast.makeText(this, "Photo updated! Remember to save changes.", Toast.LENGTH_SHORT).show()
            // TODO: Upload to Firebase Storage
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide navbar menu initially
        binding.navbar.navBarContainerLnr.visibility = View.GONE

        // Load sample data
        loadProfileData()

        // Setup RecyclerView for monthly residency hours
        monthlyResidencyAdapter = MonthlyResidencyAdapter(monthlyResidencyList)
        binding.monthlyResidencyRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.monthlyResidencyRecyclerview.adapter = monthlyResidencyAdapter

        // Setup RecyclerView for activities list
        activityAdapter = ProfileActivityAdapter(activityList)
        binding.activitiesListRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.activitiesListRecyclerview.adapter = activityAdapter

        // Show/hide nav bar options
        binding.navbar.menuIconNavImgbtn.setOnClickListener {
            val menuSection = binding.navbar.navBarContainerLnr
            menuSection.visibility =
                if (menuSection.visibility == View.VISIBLE) {
                    View.GONE
                } else View.VISIBLE
        }

        // Profile picture click - open file picker
        binding.profilePictureIv.setOnClickListener {
            changeProfilePhoto()
        }

        // Update Bio button - enter edit mode
        binding.updateBioBtn.setOnClickListener {
            enterEditMode()
        }

        // Save Bio button - save changes
        binding.saveBioBtn.setOnClickListener {
            saveBioChanges()
        }

        // Cancel Changes button - discard changes
        binding.cancelChangesBtn.setOnClickListener {
            cancelChanges()
        }

        // Return button - always goes back to Home
        binding.returnBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadProfileData() {
        // Sample data - replace with actual data from database later
        binding.profileNameTv.text = "Jules Dela Cruz (MAE)"
        binding.profileBioTv.text = "Hi! My name is Jules."
        binding.profileBioEt.setText("Hi! My name is Jules.")

        // Store original bio for cancel functionality
        originalBioText = binding.profileBioTv.text.toString()

        // Calculate monthly residency from residency history
        monthlyResidencyList = calculateMonthlyResidency()

        activityList = arrayListOf(
            "Event Activity # 4 (9/12/25)",
            "Event Activity # 3 (9/11/25)",
            "Event Activity # 2 (9/10/25)",
            "Event Activity # 1 (9/9/25)"
        )
    }

    private fun calculateMonthlyResidency(): ArrayList<MonthlyResidency> {
        // SAMPLE calculation - in real app, fetch from ResidencyHistory database
        // group by month, then sum up total minutes

        return arrayListOf(
            MonthlyResidency("Oct", 490),  // 8 hours 10 minutes = 490 minutes
            MonthlyResidency("Sep", 490),  // 8 hours 10 minutes = 490 minutes
            MonthlyResidency("Aug", 490)   // 8 hours 10 minutes = 490 minutes
        )
    }

    private fun enterEditMode() {
        // Store the original bio text before editing
        originalBioText = binding.profileBioTv.text.toString()

        // Copy current bio to EditText
        binding.profileBioEt.setText(binding.profileBioTv.text.toString())

        // Hide TextView, show EditText
        binding.profileBioTv.visibility = View.GONE
        binding.profileBioEt.visibility = View.VISIBLE

        // Hide view mode buttons, show edit mode buttons
        binding.viewModeContainer.visibility = View.GONE
        binding.editModeContainer.visibility = View.VISIBLE

        // Hide return button in edit mode
        binding.returnBtn.visibility = View.GONE

        // Focus on EditText and show keyboard
        binding.profileBioEt.requestFocus()
        binding.profileBioEt.setSelection(binding.profileBioEt.text.length) // Cursor at end
    }

    private fun saveBioChanges() {
        // Get the edited bio text
        val newBioText = binding.profileBioEt.text.toString().trim()

        if (newBioText.isEmpty()) {
            Toast.makeText(this, "Bio cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Update the TextView with new bio
        binding.profileBioTv.text = newBioText

        // TODO: Save to database
        // updateBioInDatabase(newBioText)

        // TODO: If photo was changed, upload it
        // if (selectedImageUri != null) {
        //     uploadPhotoToStorage(selectedImageUri!!)
        // }

        // Exit edit mode
        exitEditMode()

        Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun cancelChanges() {
        // Restore original bio text (discard changes)
        binding.profileBioEt.setText(originalBioText)

        // Restore original photo if it was changed
        if (selectedImageUri != null) {
            // Reload original photo from database
            // For now, just reset to default
            binding.profilePictureIv.setImageResource(R.drawable.pfp_icon_holder)
            selectedImageUri = null
        }

        // Exit edit mode without saving
        exitEditMode()

        Toast.makeText(this, "Changes cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun exitEditMode() {
        // Show TextView, hide EditText
        binding.profileBioTv.visibility = View.VISIBLE
        binding.profileBioEt.visibility = View.GONE

        // Show view mode buttons, hide edit mode buttons
        binding.viewModeContainer.visibility = View.VISIBLE
        binding.editModeContainer.visibility = View.GONE

        // Show return button again
        binding.returnBtn.visibility = View.VISIBLE
    }

    private fun changeProfilePhoto() {
        // Open file picker to select image
        pickImageLauncher.launch("image/*")
    }

    // Future function for uploading photo to Firebase Storage
    /*
    private fun uploadPhotoToStorage(imageUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference
            .child("profile_photos/$userId.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Save download URL to database
                    updatePhotoUrlInDatabase(downloadUri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload photo: ${exception.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePhotoUrlInDatabase(photoUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child("members").child(userId).child("photo")
            .setValue(photoUrl)
    }
    */

    // Future function to calculate from actual database
    private fun calculateMonthlyResidencyFromDB(): ArrayList<MonthlyResidency> {
        // Pseudocode for when you implement database:
        // 1. Fetch all ResidencyItems for this user
        // 2. Group by month (extract month from date)
        // 3. For each month group:
        //    - Parse timeIn and timeOut
        //    - Calculate duration in minutes
        //    - Sum all durations for that month
        // 4. Create MonthlyResidency objects with month name and total minutes

        return arrayListOf()
    }
}