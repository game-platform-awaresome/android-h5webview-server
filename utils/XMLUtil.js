'use strict'

function escapeHtml2(sHtml){
    console.log("escapeHtml2 sHtml = "+sHtml);
    return sHtml.replace(/[<>&"]/g,function(c){return {'<':'&lt;','>':'&gt;','&':'&amp;','"':'&quot;'}[c];});
}
exports.escapeHtml2 = escapeHtml2;