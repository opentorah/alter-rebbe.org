import CETEI from './cetei.js'

export default function loadTei(tei, thisDocument) {
    let refBehavior = [["[ref]", ["<a href='/names/$@ref.html' target='namesViewer'>", "</a>"]]];

    let CETEIcean = new CETEI();

    CETEIcean.addBehaviors({
        "tei": {
            "pb": [["[facs][n]", ["<a class='facsimileLink' target='facsimileViewer' href='../facs/" + thisDocument + ".html#p$@n'>⎙</a>"]]],
            "pb": [["[n]", ["<a class='missingFacsimileLink' target='facsimileViewer' href='../facs/" + thisDocument + ".html#p$@n'>⎙</a>"]]],
            "persName": refBehavior,
            "placeName": refBehavior,
            "orgName": refBehavior,
            "supplied": ["[", "]"]
        }
    });

    CETEIcean.getHTML5(tei, function (data) {
        document.getElementById("TEI").innerHTML = "";
        document.getElementById("TEI").appendChild(data);
        CETEIcean.addStyle(document, data);
    });
}
