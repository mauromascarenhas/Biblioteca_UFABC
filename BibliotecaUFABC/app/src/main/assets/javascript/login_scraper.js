function performLogin(username, password){
    document.getElementById('username').value = username;
    document.getElementById('password').value = password;
    document.getElementsByName('_eventId_proceed')[0].click();
}

function checkForErrors(){
    var error = document.querySelector('.form-error');
    var hasError = error != null;
    return {
        hasFormError : hasError,
        errorDetails : hasError ? error.textContent : ""
    };
}

function getUsername(){
    var logoutButton = document.querySelector("#li-logout");
    return logoutButton == null? "" : logoutButton.getElementsByTagName('span')[1].textContent.trim();
}