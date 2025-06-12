import add from './add.js';

document.getElementById('btn').addEventListener('click', function() {
    document.getElementById('h1').style.color =
        document.getElementById('h1').style.color === 'blue' ? 'red' : 'blue';
    console.log('use add import from add.js', "...")
    let a = Number.parseInt(document.getElementById('a').value);
    let b = Number.parseInt(document.getElementById('b').value);
    document.getElementById('result').innerText = add(a, b);
});