function getBookDetails(){
    var reservation_button = document.querySelector('#botaoReservar');

    var properties_list = document.querySelector('.tabelaDetalhe');
    var copies_list = document.querySelector('#exemplares');
    var media_list = document.querySelector('#midias');

    var bookDetails = {
        code : "",
        title : "",
        author : "",
        properties : null,
        media : null,
        copies : null,
        reservable : reservation_button != null,
        exists : properties_list != null
    };

    if (properties_list == null)
        return bookDetails;

    bookDetails.code = new URL(window.location.href).searchParams.get('codigo');

    var titleNode = document.querySelector('.textoDescricao');
    if (titleNode != null)
        bookDetails.title = titleNode.textContent;

    var authorNode = document.querySelector('.textoAutorDetalhe');
    if (authorNode != null)
            bookDetails.author = authorNode.textContent;

    var property_rows = properties_list.getElementsByTagName('tr');
    bookDetails.properties = new Array(property_rows.length);
    for (let i = 0; i < property_rows.length; ++i){
        let properties = {
            title : property_rows[i].getElementsByTagName('td')[0].textContent,
            description : property_rows[i].getElementsByTagName('td')[1].textContent
        };
        bookDetails.properties[i] = properties;
    }

    var media_rows = media_list.getElementsByTagName('ul');
    var media_rows_arr = new Array(media_rows.length);
    for (let i = 0; i < media_rows.length; ++i){

        let media_rows_items = media_rows[i].getElementsByTagName('li');
        let media_rows_items_arr = new Array(media_rows_items.length - 1);

        for (let j = 1; j < media_rows_items.length; ++j){
            let cAnchor = media_rows_items[j].getElementsByTagName('a')[0];

            let aProperties = {
                link : cAnchor.href,
                text : cAnchor.childNodes[0].textContent
            };

            media_rows_items_arr[j - 1] = aProperties;
        }

        let c_media_table = {
            type : media_rows_items[0].textContent,
            values : media_rows_items_arr
        };

        media_rows_arr[i] = c_media_table;
    }
    bookDetails.media = media_rows_arr;

    var copies_rows_items = copies_list.getElementsByTagName('li');
    var copies_rows_items_arr = new Array(copies_rows_items.length - 1);
    for (let i = 1; i < copies_rows_items.length; ++i){
        let lAnchor = copies_rows_items[i].getElementsByTagName('a')[0];

        let lAvailable = {
            library : lAnchor.childNodes[0].textContent,
            copies : lAnchor.childNodes[1].textContent
        };

        copies_rows_items_arr[i - 1] = lAvailable;
    }
    bookDetails.copies = copies_rows_items_arr;

    return {
        details : bookDetails,
        login: checkLoginStatus()
    };
}

function reserveBook(){
    document.querySelector('#botaoReservar').getElementsByTagName('a')[0].click();
}

function checkLoginStatus(){
    var logoutButton = document.querySelector("#li-logout");
    return logoutButton != null;
}