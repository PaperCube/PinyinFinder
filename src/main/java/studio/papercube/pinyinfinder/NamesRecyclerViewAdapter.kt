package studio.papercube.pinyinfinder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

typealias PersonMatchConsumer = (PersonMatch) -> Unit

class NamesRecyclerViewAdapter(private val context: Context)
    : RecyclerView.Adapter<NamesRecyclerViewAdapter.PersonViewHolder>() {
    private val persons = ArrayList<PersonMatch>()
    var onItemClickListener: PersonMatchConsumer? = null
    var onItemLongClickListener: PersonMatchConsumer? = null

    override fun getItemCount(): Int {
        return persons.size
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.displayPersonMatch(persons[position])
        holder.applyClickListeners(onItemClickListener, onItemLongClickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.person_view, parent, false)
        return PersonViewHolder(view)
    }

    fun commitData(newPersonList: List<PersonMatch>) {
        this.persons.clear()
        persons.addAll(newPersonList)
        notifyDataSetChanged()
    }

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val root = itemView.findViewById(R.id.person_layout_root)
        private val textView = itemView.findViewById(R.id.text_person_name) as TextView
        private lateinit var personMatch: PersonMatch

        init {
            root.isClickable = true
            root.isLongClickable = true
        }

        fun displayPersonMatch(personMatch: PersonMatch) {
            this.personMatch = personMatch
            textView.text = personMatch.toSpannableString()
        }

        fun applyClickListeners(clickListener: PersonMatchConsumer?, longClickListener: PersonMatchConsumer?) {
            if (clickListener != null) root.setOnClickListener { clickListener(personMatch) }
            if (longClickListener != null){
                root.setOnLongClickListener {
                    longClickListener(personMatch)
                    return@setOnLongClickListener true
                }
            }
        }
    }
}