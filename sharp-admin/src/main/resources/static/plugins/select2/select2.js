(function(w) {

	function select2(parent, options) {
		this.options = options
		this.valueElement = parent.firstChild
		this.inputElement = this.valueElement.nextElementSibling
		this.select2 = this.valueElement.parentNode
		this.ul = this.select2.querySelector('.items ul')

		// 注册搜索事件
		this.initEvent()
	}

	select2.prototype.initEvent = function() {
		this.inputElement.addEventListener('input', (event) => {
			this.clearItems()

			let value = event.target.value
			let result = []
			if (value) {
				result = this.options.datasource.filter(item => item.value && item.value.includes(event.target.value))

				result.forEach((item, index) => {
					let text = document.createTextNode(item.value)
					let li = document.createElement("li")
					// li.value = item.name
					li.setAttribute("value", item.name)
					if (index === 0) {
						li.className = 'active'
					}
					li.appendChild(text)
					this.ul.appendChild(li)
				})
			}

		})

		this.inputElement.addEventListener('keydown', (event) => {
			if (event.keyCode === 13) {
				for (let node of this.ul.childNodes) {
					if (node.className) {
						this.valueElement.value = node.getAttribute("value")
						this.inputElement.value = node.innerText
						break
					}
				}

				this.clearItems()
			} else if(event.keyCode === 40 || event.keyCode === 38) {
				let arrowDown = event.keyCode === 40

				for (let node of this.ul.childNodes) {
					if (node.className) {
						node.className = ''

						let activeNode = arrowDown ? node.nextSibling : node.previousSibling
						if(!activeNode) {
							activeNode = arrowDown ? this.ul.childNodes[0] : this.ul.childNodes[this.ul.childNodes.length - 1]
						}

						activeNode.className = 'active'
						this.valueElement.value = activeNode.getAttribute("value")
						this.inputElement.value = activeNode.innerText
						break
					}
				}
			}
		})

		this.ul.addEventListener('click', (event) => {
			this.valueElement.value = event.target.getAttribute("value")
			this.inputElement.value = event.target.innerText
			this.clearItems()
		})

		this.inputElement.addEventListener('blur', () => {
			setTimeout(() => this.clearItems(), 100)
		})

		this.ul.addEventListener('mouseover', (event) => {
			this.ul.childNodes.forEach(node => {
				node.className = ''
			})

			event.target.className = 'active'
		})
	}

	select2.prototype.clearItems = function() {
		while (this.ul.firstChild) this.ul.removeChild(this.ul.firstChild);
	}

	w.select2 = select2
})(window)