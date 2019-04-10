// Get books available for renewal
function getRenewals(){
    var errorDiv = document.querySelector('.divErroCentralizada');
    if (errorDiv != null){
        js_api.setUsernameErr(errorDiv.getElementsByTagName('b')[0].textContent);
        return;
    }

    var listContent = document.querySelector('ul[data-theme=c]');
    var book_structures = listContent.getElementsByTagName('li');

    var books = new Array(book_structures.length - 1);
    for (let i = 1; i < book_structures.length; ++i){
        let cBook = book_structures[i];

        let cProperties = {
            title : cBook.getElementsByTagName('h3')[0].textContent,
            patrimony : cBook.getElementsByTagName('p')[0].textContent,
            library : cBook.getElementsByTagName('p')[1].textContent,
            date : cBook.getElementsByTagName('p')[2].textContent,
            renewal_link : cBook.querySelector('a.botaoFechar').href
        };

        books[i-1] = cProperties;
    }

    js_api.setRenewalBooks(JSON.stringify(books));
}