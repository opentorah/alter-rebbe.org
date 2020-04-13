import CETEI from './cetei.js'

export default function loadTei(tei) {
    let wrapper = document
        .getElementsByClassName("page-content").item(0)
        .getElementsByClassName("wrapper").item(0);

    let teiDiv = document.createElement("div");
    teiDiv.innerHTML = "This page will only work in modern browsers.";
    wrapper.appendChild(teiDiv);

    let refBehavior = [["[ref]", ["<a href='/names/$@ref.html' target='namesViewer'>", "</a>"]]];

    let CETEIcean = new CETEI();

    CETEIcean.addBehaviors({
        "tei": {
            "pb": [["[role][target]", ["<a target='$@role' href='$@target'>⎙</a>"]]],
            "persName": refBehavior,
            "placeName": refBehavior,
            "orgName": refBehavior,
            "supplied": ["[", "]"]
        }
    });

    CETEIcean.getHTML5(tei, function (data) {
        teiDiv.innerHTML = "";
        teiDiv.appendChild(data);
        CETEIcean.addStyle(document, data);
    });
}
