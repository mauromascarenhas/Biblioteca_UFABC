// Get reserved books
function getReservations(){
    var errorDiv = document.querySelector('.divErroCentralizada');
    if (errorDiv != null){
        return{
            hasErrorDiv : true,
            username : errorDiv.getElementsByTagName('b')[0].textContent
        };
    }

    var listContent = document.querySelector('ul[data-theme=c]');
    var book_structures = listContent.getElementsByTagName('li');

    var books = new Array(book_structures.length - 1);
    for (let i = 1; i < book_structures.length; ++i){
        let cBook = book_structures[i];

        let cProperties = {
            title : cBook.getElementsByTagName('h3')[0].textContent,
            queue : cBook.getElementsByTagName('p')[0].textContent,
            material : cBook.getElementsByTagName('p')[1].textContent,
            library : cBook.getElementsByTagName('p')[2].textContent,
            situation : cBook.getElementsByTagName('p')[3].textContent,
            cancel_link : cBook.querySelector('a.botaoFechar').href
        };

        books[i-1] = cProperties;
    }

    return {
        hasErrorDiv : false,
        reservationBooks : books
    };
}

// Get message displayed after cancelling a reservation
function getCancellationMessage(){
    let value = document.querySelector('div.center').getElementsByTagName('span')[0].textContent;
    document.querySelector('div.center').getElementsByTagName('a')[0].click();
    return value;
}