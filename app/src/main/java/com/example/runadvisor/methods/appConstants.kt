package com.example.runadvisor.methods

import com.example.runadvisor.R

const val ALL_PERMISSIONS_CHECKED = 2
const val PERMISSION_NOT_OK = 0
const val PERMISSION_OK = 1
const val DATA_PERMISSIONS_CODE = 98
const val LOCATION_PERMISSION_CODE = 99

const val GALLERY_REQUEST_CODE = 102
const val PICK_IMAGE = 1

const val MIN_TRACK_LENGTH:Double = 1000.0

const val URL_TIMER:Long = 1500

const val INCREASE_POINTS = 10
const val MAX_POINTS = 1280
const val MAX_STORAGE_LENGTH:Double = 42000.0

const val SORT_TIME_OUT = 2000

const val MIN_LATITUDE = 90.0
const val MAX_LATITUDE = -90.0
const val MIN_LONGITUDE = 180.0
const val MAX_LONGITUDE = -180.0
const val DP_TILE_SIZE = 256.0
const val LN2 = 0.6931471805599453

const val IMAGE_PATH = "images/"
const val USER_COLLECTION = "Users"
const val ITEM_COLLECTION = "RunItems"

const val SELECT_IMAGE_PATH = "image/*"

val authErrors = mapOf("ERROR_INVALID_CUSTOM_TOKEN" to R.string.error_login_custom_token,
    "ERROR_CUSTOM_TOKEN_MISMATCH" to R.string.error_login_custom_token_mismatch,
    "ERROR_INVALID_CREDENTIAL" to R.string.error_login_credential_malformed_or_expired,
    "ERROR_INVALID_EMAIL" to R.string.error_login_invalid_email,
    "ERROR_WRONG_PASSWORD" to R.string.error_login_wrong_password,
    "ERROR_USER_MISMATCH" to R.string.error_login_user_mismatch,
    "ERROR_REQUIRES_RECENT_LOGIN" to R.string.error_login_requires_recent_login,
    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" to R.string.error_login_accounts_exits_with_different_credential,
    "ERROR_EMAIL_ALREADY_IN_USE" to  R.string.error_login_email_already_in_use,
    "ERROR_CREDENTIAL_ALREADY_IN_USE" to R.string.error_login_credential_already_in_use,
    "ERROR_USER_DISABLED" to R.string.error_login_user_disabled,
    "ERROR_USER_TOKEN_EXPIRED" to R.string.error_login_user_token_expired,
    "ERROR_USER_NOT_FOUND" to R.string.error_login_user_not_found,
    "ERROR_INVALID_USER_TOKEN" to R.string.error_login_invalid_user_token,
    "ERROR_OPERATION_NOT_ALLOWED" to R.string.error_login_operation_not_allowed,
    "ERROR_WEAK_PASSWORD" to R.string.error_login_password_is_weak)

