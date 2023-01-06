$('div').hide()
$('#table').change(function(){
    let v = this.value;
    $('div').hide().filter(function(){return this.id===v}).show()
});
$('div').hide()
$('#operation').change(function(){
    let v = this.value;
    $('div').hide().filter(function(){return this.id===v}).show()
});