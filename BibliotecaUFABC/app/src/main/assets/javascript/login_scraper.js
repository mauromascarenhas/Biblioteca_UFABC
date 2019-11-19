function performLogin(username, password){
    document.getElementById('login').value = username;
    document.getElementById('senha').value = password;
    document.getElementById('botao_autenticar').click();
}

function checkForErrors(){
    var error = document.querySelector('#informacao');
    var hasError = error.textContent != '';
    return {
        hasFormError : hasError,
        errorDetails : hasError ? error.textContent : ""
    };
}

function getUsername(){
    var logoutButton = document.querySelector("#li-logout");
    return logoutButton == null? "" : logoutButton.getElementsByTagName('span')[1].textContent.trim();
}