<!doctype html>
<html lang="en">
  <head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

    <!-- Optional theme -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">

    <!-- Optional JavaScript -->
    <!-- jQuery first, then Popper.js, then Bootstrap JS -->
    <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js" integrity="sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1" crossorigin="anonymous"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js" integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM" crossorigin="anonymous"></script>
    <!--<script type="text/javascript" src='./script.js'></script>-->
    <title>LucDeb</title>
    
    <style>
        #heading{
            text-align: center;
            font-family: Cambria, Cochin, Georgia, Times, 'Times New Roman', serif;
        }
    </style>

    </head>

    <body>
        <div class="container heading" id="heading">
            <h3>LucDeb : An Explainer for Lucene Index</h3>
            <p>Lucene version : 5.3.1</p>
        </div>
        <div class="container">
            <div class="row">

                <div class="col-5 col-lg-5 col-xl-5 col-md-5 col-sm-5">
                    <form id = "inputs">
                        <div class="form-group">
                            <label for="index">Collection </label>
                            <select name="index" class="form-control index">
                                <option disabled selected value> -- Select A Collection -- </option>
                                <option value='trec123'>TREC-123</option>
                                <option value='trec678'>TREC-678</option>
                                <option value='wt10g'>WebTrack 10G</option>
                                <option value='gov2'> Gov2 </option>
                                <option value='clueweb'>ClueWeb 09B</option>
                            </select>
                        </div>
                        <br/><br/>  
                        <button class="btn btn-success btn-submit" type="button" id="submit"> Go </button>
                    </form>

                </div>

                <div class="col-7 col-lg-7 col-xl-7 col-md-7 col-sm-7">
                    <div class="panel panel-info" style="position: relative; top: 20px;">
                            <div class="panel-heading">
                                <h5 class="panel-title">Collection Information</h5>
                            </div>
                            <div class="panel-body" id="colInfo" style="display:none">
                                <p id="colInfo"></p>
                            </div>
                        </div>
                </div>

            </div>
        </div>


    </body>
    <script>
    var colInfo = {};
    colInfo['trec123'] = "<b>TREC-123</b><br/>Used in TREC 1, 2 and 3 Ad-hoc Track.<br/><br/>\
                        <table class='table table-striped'>\
                        <tr><td>Number of Documnents</td> <td>741,856</td></tr>\
                        <tr><td>Number of Topics </td><td> 150 (51-200)</td></tr>\
                        <tr><td>Number of Relevant Docs </td><td>37836</td></tr>\
                        </table>";
    
    colInfo['trec678'] = "<b>TREC-678</b><br/>Used in TREC 6, 7 and 8 Ad-hoc Track.<br/><br/>\
    <table class='table table-striped'>\
    <tr><td>Number of Documnents</td> <td>528,155</td></tr>\
    <tr><td>Number of Topics </td><td> 250 (301-450) & (601-700)</td></tr>\
    <tr><td>Number of Relevant Docs </td><td>17412</td></tr>\
    </table>";

    colInfo['wt10g'] = "<b>WT10G</b><br/>Used in TREC 9 & 10 Ad-hoc Track.<br/><br/>\
    <table class='table table-striped'>\
    <tr><td>Number of Documnents</td> <td>1,692,096</td></tr>\
    <tr><td>Number of Topics </td><td> 100 (451-550)</td></tr>\
    <tr><td>Number of Relevant Docs </td><td>5980</td></tr>\
    </table>";

    colInfo['gov2'] = "<b>GOV2</b><br/>Collection of webpages of .gov domain. Used in TREC Terabyte track.<br/><br/>\
    <table class='table table-striped'>\
    <tr><td>Number of Documnents</td> <td>25,205,179</td></tr>\
    <tr><td>Number of Topics </td><td> 150 (701-850)</td></tr>\
    <tr><td>Number of Relevant Docs </td><td>26917</td></tr>\
    </table>";
    
    colInfo['clueweb'] = "<b>ClueWeb09B</b><br/>Collection of web documents in English. Used in TREC Web track. [2009-2012]<br/><br/>\
    <table class='table table-striped'>\
    <tr><td>Number of Documnents</td> <td>50,220,423</td></tr>\
    <tr><td>Number of Topics </td><td> 200 (1-200)</td></tr>\
    <tr><td>Number of Relevant Docs </td><td>11037</td></tr>\
    </table>";
    

    $(document).ready(function(){
        
        $('.index').change(function(){
            var selection = $('.index').val();
            console.log(selection)
            $('#colInfo').hide();
            $('#colInfo').html(colInfo[selection]);
            $('#colInfo').show();
        });

        $('#submit').click(function(){
            var selection = $('.index').val();
            var data = $("#inputs").serializeArray();
            var obj = {};
            for (var a = 0; a < data.length; a++) {
                obj[data[a].name] = data[a].value;
            }
            var jsonData = JSON.stringify(obj);
            console.log(jsonData);
            console.log(encodeURIComponent(jsonData));
            $.ajax({
                type: "GET",               
                url : "webapi/resource/index?index="+selection,
                //data : jsonData,
                //dataTye: 'json',
                success : function(data){
                    console.log(data);
                    $(location).attr('href','lucdeb.html');
                },
                error : function(){
                    console.log("ERROR : Cannot Get Data");
                }
            });
        });
    });
    </script>
</html>