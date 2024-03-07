package com.wordle.client.entity

class Request(
    // ALL VARIABLE BELOW REFER TO DATA FROM JSON FILE: SEE README.md

    // SUCCESS OR FAILURE
    var result: String,

    // LAST TIME DATA WAS UPDATED
    var time_last_update_utc: String,

    // NEXT TIME DATA WILL BE UPDATED
    var time_next_update_utc: String,

    // IMPLEMENT BASE CODE: USD = US DOLLARS
    var base_code: String,

    // CURRENT RATE OF CURRENCY
    var rates: Currency
)